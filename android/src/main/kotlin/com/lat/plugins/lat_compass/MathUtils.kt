package com.lat.plugins.lat_compass


import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import com.lat.plugins.lat_compass.DisplayRotation.*
import kotlin.math.roundToInt

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(rotationVector: RotationVector): Azimuth? {
        val rotationMatrix = getRotationMatrix(rotationVector)
        val orientationInRadians = SensorManager.getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        if (!azimuthInDegrees.isFinite()) {
            return null
        }
        return Azimuth(azimuthInDegrees)
    }

    private fun getRotationMatrix(rotationVector: RotationVector): FloatArray {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return rotationMatrix
    }

    @JvmStatic
    fun getMagneticDeclination(location: Location): Float {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()
        val time = location.time
        val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
        return geomagneticField.declination
    }


}