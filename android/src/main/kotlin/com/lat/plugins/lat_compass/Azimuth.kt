package com.lat.plugins.lat_compass


import kotlin.math.roundToInt

class Azimuth(_degrees: Float) {

    init {
        if (!_degrees.isFinite()) {
            throw IllegalArgumentException("Degrees must be finite but was '$_degrees'")
        }
    }

    val degrees = normalizeAngle(_degrees)

    val roundedDegrees = normalizeAngle(_degrees.roundToInt().toFloat()).toInt()

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    override fun toString(): String {
        return "Azimuth(degrees=$degrees)"
    }

    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    operator fun minus(degrees: Float) = Azimuth(this.degrees - degrees)

    operator fun compareTo(azimuth: Azimuth) = this.degrees.compareTo(azimuth.degrees)
}

private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)

private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)