package org.edgepose.android

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.nio.ByteBuffer

class YuvFramePreprocessorTest {
    @Test fun neutralChromaProducesGrayscaleRgb() {
        val result = YuvFramePreprocessor.convert(
            width = 2, height = 2,
            y = ByteBuffer.wrap(byteArrayOf(16, 235.toByte(), 81, 145.toByte())),
            u = ByteBuffer.wrap(byteArrayOf(128.toByte())),
            v = ByteBuffer.wrap(byteArrayOf(128.toByte())),
            yRowStride = 2, uvRowStride = 1, uvPixelStride = 1,
            rotationDegrees = 0, outputSize = 2,
        )
        assertArrayEquals(byteArrayOf(0, 0, 0, 255.toByte(), 255.toByte(), 255.toByte(), 76, 76, 76, 150.toByte(), 150.toByte(), 150.toByte()), result)
    }

    @Test fun clockwiseRotationMapsOutputTopLeftToSourceBottomLeft() {
        val result = YuvFramePreprocessor.convert(
            width = 2, height = 2,
            y = ByteBuffer.wrap(byteArrayOf(16, 235.toByte(), 81, 145.toByte())),
            u = ByteBuffer.wrap(byteArrayOf(128.toByte())),
            v = ByteBuffer.wrap(byteArrayOf(128.toByte())),
            yRowStride = 2, uvRowStride = 1, uvPixelStride = 1,
            rotationDegrees = 90, outputSize = 2,
        )
        assertArrayEquals(byteArrayOf(76, 76, 76), result.copyOfRange(0, 3))
    }
}
