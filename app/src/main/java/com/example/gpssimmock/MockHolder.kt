package com.example.gpssimmock

import android.location.Location
import android.os.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient

/** Estado compartido: última coordenada del simulador, con velocidad y rumbo calculados. */
object MockHolder {
    @Volatile var fused: FusedLocationProviderClient? = null
    @Volatile var location: Location? = null
    @Volatile var active: Boolean = false

    fun update(lat: Double, lon: Double) {
        val now = SystemClock.elapsedRealtimeNanos()
        val prev = location
        val l = Location("gps").apply {
            latitude = lat
            longitude = lon
            accuracy = 3.0f
            altitude = 2240.0          // altitud aprox. del Valle de México
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = now
        }
        if (prev != null && active) {
            val dt = (now - prev.elapsedRealtimeNanos) / 1_000_000_000f
            if (dt > 0.15f) {
                val dist = prev.distanceTo(l)          // metros (horizontal)
                speed = (dist / dt).coerceIn(0f, 40f)  // m/s, máx ~144 km/h
                bearing = prev.bearingTo(l)            // rumbo en grados
            }
        }
        location = l
    }
}
