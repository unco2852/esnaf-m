package com.example.esnafm.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

data class DeviceCoordinates(val latitude: Double, val longitude: Double)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
fun requestDeviceLocation(context: Context, onResult: (DeviceCoordinates?) -> Unit) {
    if (!hasLocationPermission(context)) {
        onResult(null)
        return
    }

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null) {
        onResult(null)
        return
    }

    val hasFinePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val provider = when {
        hasFinePermission && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        hasFinePermission && manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
        else -> null
    }

    if (provider == null) {
        onResult(null)
        return
    }

    val completed = AtomicBoolean(false)
    val recentLocation = try {
        manager.getLastKnownLocation(provider)
    } catch (_: SecurityException) {
        null
    }
    if (recentLocation != null && System.currentTimeMillis() - recentLocation.time <= RECENT_LOCATION_MAX_AGE_MS) {
        finishLocation(completed, recentLocation, onResult)
        return
    }

    val handler = Handler(Looper.getMainLooper())
    fun finish(location: Location?) {
        if (completed.compareAndSet(false, true)) {
            val result = location?.let { DeviceCoordinates(it.latitude, it.longitude) }
            onResult(result)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val cancellationSignal = CancellationSignal()
        handler.postDelayed({
            if (completed.compareAndSet(false, true)) {
                cancellationSignal.cancel()
                val lastKnown = try {
                    manager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
                onResult(lastKnown?.let { DeviceCoordinates(it.latitude, it.longitude) })
            }
        }, LOCATION_TIMEOUT_MS)

        try {
            manager.getCurrentLocation(provider, cancellationSignal, ContextCompat.getMainExecutor(context)) { location ->
                finish(location)
            }
        } catch (_: SecurityException) {
            finish(null)
        } catch (_: IllegalArgumentException) {
            finish(null)
        }
        return
    }

    lateinit var listener: LocationListener
    listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            try {
                manager.removeUpdates(this)
            } catch (_: SecurityException) {
                // Permission may have been revoked while the request was running.
            }
            finish(location)
        }

        @Deprecated("Deprecated by Android; retained for API 24–28 compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) = Unit

        override fun onProviderDisabled(provider: String) {
            try {
                manager.removeUpdates(this)
            } catch (_: SecurityException) {
                // Permission may have been revoked while the request was running.
            }
            finish(null)
        }
    }

    handler.postDelayed({
        if (completed.compareAndSet(false, true)) {
            try {
                manager.removeUpdates(listener)
            } catch (_: SecurityException) {
                // Permission may have been revoked while the request was running.
            }
            val lastKnown = try {
                manager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
            onResult(lastKnown?.let { DeviceCoordinates(it.latitude, it.longitude) })
        }
    }, LOCATION_TIMEOUT_MS)

    try {
        manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
    } catch (_: SecurityException) {
        finish(null)
    } catch (_: IllegalArgumentException) {
        finish(null)
    }
}

private const val LOCATION_TIMEOUT_MS = 12_000L
private const val RECENT_LOCATION_MAX_AGE_MS = 60_000L

private fun finishLocation(
    completed: AtomicBoolean,
    location: Location,
    onResult: (DeviceCoordinates?) -> Unit
) {
    if (completed.compareAndSet(false, true)) {
        onResult(DeviceCoordinates(location.latitude, location.longitude))
    }
}
