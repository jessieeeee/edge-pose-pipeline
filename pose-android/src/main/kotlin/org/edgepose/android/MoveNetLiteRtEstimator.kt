package org.edgepose.android

import org.edgepose.core.KeypointType
import org.edgepose.core.Landmark
import org.edgepose.core.Point2D
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

/** Runs MoveNet Thunder on an already prepared 256x256 RGB UINT8 tensor. */
class MoveNetLiteRtEstimator(
    model: ByteBuffer,
    private val numThreads: Int = 2,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)
    private val interpreter: Interpreter
    private val inputBytes: Int
    private val input: ByteBuffer
    private val output: Any

    init {
        require(numThreads > 0)
        val created = Interpreter(model, Interpreter.Options().setNumThreads(numThreads))
        try {
            val inputTensor = created.getInputTensor(0)
            require(inputTensor.dataType() == DataType.UINT8) { "MoveNet input must be UINT8" }
            require(inputTensor.shape().contentEquals(intArrayOf(1, 256, 256, 3))) {
                "Expected input shape [1, 256, 256, 3]"
            }
            val outputTensor = created.getOutputTensor(0)
            require(outputTensor.dataType() == DataType.FLOAT32) { "MoveNet output must be FLOAT32" }
            val outputShape = outputTensor.shape()
            require(
                outputShape.contentEquals(intArrayOf(1, 17, 3)) ||
                    outputShape.contentEquals(intArrayOf(1, 1, 17, 3)),
            ) { "Expected output shape [1, 17, 3] or [1, 1, 17, 3]" }
            interpreter = created
            inputBytes = inputTensor.numBytes()
            input = ByteBuffer.allocateDirect(inputBytes).order(ByteOrder.nativeOrder())
            output = if (outputShape.size == 3) {
                Array(1) { Array(17) { FloatArray(3) } }
            } else {
                Array(1) { Array(1) { Array(17) { FloatArray(3) } } }
            }
        } catch (failure: Throwable) {
            created.close()
            throw failure
        }
    }

    @Synchronized
    fun estimate(rgbBytes: ByteArray): Map<KeypointType, Landmark> {
        check(!closed.get()) { "estimator is closed" }
        require(rgbBytes.size == inputBytes) { "Expected $inputBytes RGB bytes, got ${rgbBytes.size}" }
        input.clear()
        input.put(rgbBytes)
        input.rewind()
        interpreter.run(input, output)
        val rows = when (val tensorOutput = output) {
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                val first = tensorOutput[0]
                if (first is Array<*> && first[0] is FloatArray) {
                    (first as Array<FloatArray>).map { it.copyOf() }
                } else {
                    @Suppress("UNCHECKED_CAST")
                    ((first as Array<*>)[0] as Array<FloatArray>).map { it.copyOf() }
                }
            }
            else -> error("Unsupported MoveNet output buffer")
        }
        return MoveNetOutputMapper.map(rows)
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) interpreter.close()
    }

    companion object {
        /** Loads a user-provided model file without bundling or redistributing model bytes. */
        fun fromModelFile(modelFile: File, numThreads: Int = 2): MoveNetLiteRtEstimator {
            require(modelFile.isFile) { "MoveNet model file does not exist: ${modelFile.name}" }
            val modelBuffer = RandomAccessFile(modelFile, "r").use { file ->
                file.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            }
            return MoveNetLiteRtEstimator(modelBuffer, numThreads)
        }
    }
}
