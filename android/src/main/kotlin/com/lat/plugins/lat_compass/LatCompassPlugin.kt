package com.lat.plugins.lat_compass

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry


private const val LOCATION_UPDATES_MIN_TIME_MS = 1_000L
private const val LOCATION_UPDATES_MIN_DISTANCE_M = 10.0f

/** LatCompassPlugin */
class LatCompassPlugin : FlutterPlugin, MethodCallHandler, StreamHandler, PluginRegistry.RequestPermissionsResultListener, ActivityAware {
    private val TAG = "LatCompass"

    private var currentActivity: Activity? = null

    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null

    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null

    private var trueAzimuth: Azimuth? = null
    private var magneticAzimuth: Azimuth? = null
    private var azimuthAccuracy: Int? = null
    private var location: Location? = null

    private var latestData: List<Double>? = null

    private val compassSensorEventListener = CompassSensorEventListener()
    private val compassLocationListener = CompassLocationListener()

    private  fun log(value: String) {
//        Log.d(TAG, value)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        currentActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        currentActivity = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        log( "onRequestPermissionsResult $requestCode, $permissions, $grantResults, ${permissions[0]}, ${grantResults[0]}")
        if (requestCode == -1) return false
        if (permissions.isEmpty()) return false
        if (grantResults.isEmpty()) return false
        if (checkCoarseLocationPermission()) {
            log( "onRequestPermissionsResult: true")
            registerCoarseLocationListener()
            return true
        }
        log( "onRequestPermissionsResult: false")
        return false
    }


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "lat_compass/event")
        eventChannel.setStreamHandler(this)

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "lat_compass")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        log( "onMethodCall")
        result.notImplemented()
    }


    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
        registerSensorListener()
        if (checkCoarseLocationPermission()) {
            registerCoarseLocationListener()
        }
    }

    override fun onCancel(arguments: Any?) {
        sensorManager?.unregisterListener(compassSensorEventListener)
        locationManager?.removeUpdates(compassLocationListener)

        eventSink = null
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
    }

    private fun checkCoarseLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
                return true
            }
        } else {
            if (context.packageManager.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context.packageName) == PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    @RequiresPermission(value = ACCESS_COARSE_LOCATION)
    private fun registerCoarseLocationListener() {
        log( "registerCoarseLocationListener")
        locationManager?.also { locationManager ->
            val criteria = getLocationManagerCriteria()
            val bestProvider = locationManager.getBestProvider(criteria, true)
            log( "bestProvider: $bestProvider")

            bestProvider?.also { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    LOCATION_UPDATES_MIN_TIME_MS,
                    LOCATION_UPDATES_MIN_DISTANCE_M,
                    compassLocationListener
                )
            } ?: run {
                log( "No LocationProvider available")
            }
        } ?: run {
            log( "LocationManager not present")
        }
    }

    private fun getLocationManagerCriteria(): Criteria {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.powerRequirement = Criteria.POWER_HIGH
        return criteria
    }

    private fun registerSensorListener() {
        sensorManager?.also { sensorManager ->
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?.also { rotationVectorSensor ->
                    val success = sensorManager.registerListener(
                        compassSensorEventListener,
                        rotationVectorSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                    if (success) {
                        log( "Registered listener for rotation vector sensor")
                    } else {
                        log( "Could not enable rotation vector sensor")
                    }
                } ?: run {
                log("Rotation vector sensor not available")
            }
        } ?: run {
            log( "SensorManager not present")
        }
    }

    private inner class CompassLocationListener : LocationListener {
        override fun onLocationChanged(loc: Location) {
            log("Location changed to $loc")
            location = loc
        }
    }

    private inner class CompassSensorEventListener : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            when (sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> setSensorAccuracy(accuracy)
                else -> log("Unexpected accuracy changed event of type ${sensor.type}")
            }
        }

        private fun setSensorAccuracy(accuracy: Int) {
            azimuthAccuracy = accuracy
        }

//        private fun adaptSensorAccuracy(accuracy: Int): SensorAccuracy {
//            return when (accuracy) {
//                SensorManager.SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
//                SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
//                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
//                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
//                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
//                else -> {
//                    log("Encountered unexpected sensor accuracy value '$accuracy'")
//                    SensorAccuracy.NO_CONTACT
//                }
//            }
//        }

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
                else -> log( "Unexpected sensor changed event of type ${event.sensor.type}")
            }
        }

        private fun updateCompass(event: SensorEvent) {
            if (azimuthAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || azimuthAccuracy == SensorManager.SENSOR_STATUS_NO_CONTACT) {
                return
            }

            val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
            val result  = MathUtils.calculateAzimuth(rotationVector) ?: return
            magneticAzimuth = result

            val magneticDeclination = getMagneticDeclination()
            trueAzimuth = magneticAzimuth!!.plus(magneticDeclination)

            if (magneticAzimuth != null) {
                val m = magneticAzimuth!!.degrees.toDouble()
                val t = trueAzimuth!!.degrees.toDouble()
                val a = azimuthAccuracy?.toDouble() ?: -1.0
                val data = listOf(m, t, a)

                if (latestData == data) return;
                latestData = data
                eventSink?.success(data)
            }

//            log( "${magneticAzimuth!!.roundedDegrees} ${trueAzimuth?.roundedDegrees} $azimuthAccuracy ")
        }

        private fun getMagneticDeclination(): Float {
            return location
                ?.let(MathUtils::getMagneticDeclination)
                ?: 0.0f
        }
    }



}
