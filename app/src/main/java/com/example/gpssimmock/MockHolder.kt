package com.example.gpssimmock

import android.location.Location
import android.location.LocationManager
import android.os.SystemClock

/** Estado compartido: última coordenada del simulador, con velocidad y rumbo calculados. */
object MockHolder {
    @Volatile var lm: LocationManager? = null
    @Volatile var location: Location? = null
    @Volatile var active: Boolean = false

    fun update(lat: Double, lon: Double) {
        val now = SystemClock.elapsedRealtimeNanos()
        val prev = location
        val l = Location(LocationManager.GPS_PROVIDER)
        l.latitude = lat
        l.longitude = lon
        l.accuracy = 3.0f
        l.altitude = 2240.0          // altitud: cámbiala si quieres
        l.time = System.currentTimeMillis()
        l.elapsedRealtimeNanos = now
        if (prev != null && active) {
            val dt = (now - prev.elapsedRealtimeNanos) / 1_000_000_000f
            if (dt > 0.15f) {
                val dist = prev.distanceTo(l)
                l.speed = (dist / dt).coerceIn(0f, 40f)
                l.bearing = prev.bearingTo(l)
            }
        }
        location = l
    }
}
