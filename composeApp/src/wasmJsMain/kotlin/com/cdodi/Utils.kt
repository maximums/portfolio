package com.cdodi

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.isActive
import org.jetbrains.skia.Data
import org.jetbrains.skiko.currentNanoTime

private const val ONE_SECOND_NANOS = 1_000_000_000f

val iTime: State<Float>
    @Composable get() = produceState(0f) {
        val startTime = currentNanoTime()

        while (isActive) {
            withInfiniteAnimationFrameNanos {
                value = (it - startTime) / ONE_SECOND_NANOS
            }
        }
    }

// only support Int, Float and Float is interpreted as a 32-bits Int `Float.toBits()`
fun uniformData(vararg data: Number): Data {
    return ByteArray(size = data.sumOf { Int.SIZE_BYTES }).run {
        data.forEachIndexed { index, number -> number.populate(array = this, offset = index * Int.SIZE_BYTES ) }
        Data.makeFromBytes(bytes = this)
    }
}

private fun Number.populate(array: ByteArray, offset: Int) {
    val number = when (this) {
        is Int -> this
        is Float -> toBits()
        else -> TODO("Why?")
    }

    repeat(Int.SIZE_BYTES) { byteIdx ->
        array[byteIdx + offset] = (number shr (byteIdx * Byte.SIZE_BITS)).toByte()
    }
}