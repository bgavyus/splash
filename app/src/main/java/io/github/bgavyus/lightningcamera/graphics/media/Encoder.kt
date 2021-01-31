package io.github.bgavyus.lightningcamera.graphics.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Size
import io.github.bgavyus.lightningcamera.common.DeferScope
import io.github.bgavyus.lightningcamera.common.SingleThreadHandler
import io.github.bgavyus.lightningcamera.extensions.android.media.EncoderEvent
import io.github.bgavyus.lightningcamera.extensions.android.media.configureEncoder
import io.github.bgavyus.lightningcamera.extensions.android.media.encoderEvents
import io.github.bgavyus.lightningcamera.extensions.containsFlags
import io.github.bgavyus.lightningcamera.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

class Encoder(size: Size, framesPerSecond: Int) : DeferScope() {
    companion object {
        private const val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
    }

    private val handler = SingleThreadHandler(javaClass.simpleName)
        .apply { defer(::close) }

    private val scope = CoroutineScope(handler.asCoroutineDispatcher(javaClass.simpleName))
        .apply { defer(::cancel) }

    private val _format = MutableStateFlow(null as MediaFormat?)
    val format = _format.asStateFlow()

    private val _samples = MutableSharedFlow<Sample>()
    val samples = _samples.asSharedFlow()

    private val codec = MediaCodec.createEncoderByType(mimeType).apply {
        defer(::release)

        encoderEvents(handler).onEach {
            when (it) {
                is EncoderEvent.FormatChanged -> onFormatChanged(it.format)
                is EncoderEvent.BufferAvailable -> onBufferAvailable(it.index, it.info)
            }
        }
            .launchIn(scope)

        val format = FormatFactory.create(size, framesPerSecond, mimeType)
        configureEncoder(format)
    }

    val surface = codec.createInputSurface()
        .apply { defer(::release) }

    init {
        codec.apply {
            start()
            defer(::stop)
            defer(::flush)
        }
    }

    private fun onFormatChanged(format: MediaFormat) {
        Logger.log("Format available")
        _format.value = format
    }

    private suspend fun onBufferAvailable(index: Int, info: MediaCodec.BufferInfo) {
        try {
            if (info.flags containsFlags MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                Logger.log("Got codec config")
                return
            }

            if (info.flags containsFlags MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                throw RuntimeException()
            }

            val buffer = codec.getOutputBuffer(index)
                ?: throw RuntimeException()

            _samples.emit(Sample(buffer, info))
        } finally {
            codec.releaseOutputBuffer(index, false)
        }
    }
}
