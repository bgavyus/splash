package io.github.bgavyus.lightningcamera.graphics.media

import android.media.MediaCodec
import java.nio.ByteBuffer

class Sample(maxSize: Int) {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(maxSize)
    val info = MediaCodec.BufferInfo()

    fun copyFrom(otherBuffer: ByteBuffer, otherInfo: MediaCodec.BufferInfo) {
        buffer.copyFrom(otherBuffer)
        info.copyFrom(otherInfo)
    }
}

fun ByteBuffer.copyFrom(other: ByteBuffer) {
    position(other.position())
    limit(other.limit())
    put(other)
}

fun MediaCodec.BufferInfo.copyFrom(other: MediaCodec.BufferInfo) =
    set(other.offset, other.size, other.presentationTimeUs, other.flags)