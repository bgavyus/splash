package io.github.bgavyus.splash.graphics.media

import android.media.MediaCodec
import android.util.Log
import io.github.bgavyus.splash.common.Deferrer
import io.github.bgavyus.splash.common.Snake
import java.nio.ByteBuffer

class SamplesSnake(sampleSize: Int, samplesCount: Int): Deferrer() {
    companion object {
        private val TAG = Recorder::class.simpleName
    }

    private val snake = Snake(Array(samplesCount) { Sample(sampleSize) }.apply {
        defer {
            Log.d(TAG, "Freeing samples")
            forEach { it.close() }
        }
    })

    fun feed(buffer: ByteBuffer, info: MediaCodec.BufferInfo) = snake.feed { sample ->
        sample.copyFrom(buffer, info)
    }

    fun drain(block: (ByteBuffer, MediaCodec.BufferInfo) -> Unit) {
        var reachedKeyFrame = false

        snake.drain { sample ->
            if (reachedKeyFrame || sample.info.keyFrame) {
                reachedKeyFrame = true
                block(sample.buffer, sample.info)
            }
        }
    }
}
