package com.example.gpssimmock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log

class MockService : Service() {

    private var running = false
    private var worker: Thread? = null
    private var providerAdded = false
    private val nm by lazy { getSystemService(NotificationManager::class.java) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        MockHolder.lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun notificar(texto: String) {
        val n = Notification.Builder(this, "mock")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("GPS Simulador")
            .setContentText(texto)
            .setOngoing(true)
            .build()
        nm.notify(1, n)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()
        startForeground(1, Notification.Builder(this, "mock")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("GPS Simulador")
            .setContentText("Iniciando…")
            .setOngoing(true)
            .build())

        if (!running) {
            running = true
            MockHolder.active = true
            worker = Thread {
                while (running) {
                    val lm = MockHolder.lm ?: break

                    if (!providerAdded) {
                        try {
                            lm.addTestProvider(
                                LocationManager.GPS_PROVIDER,
                                false, false, false, false,
                                true, true, true,
                                Criteria.POWER_LOW, Criteria.ACCURACY_FINE
                            )
                            providerAdded = true
                            Log.i("MockService", "addTestProvider OK")
                        } catch (se: SecurityException) {
                            Log.e("MockService", "SecurityException: no eres app de ubicación ficticia")
                            notificar("ERROR: activa 'ubicación ficticia' en Opciones de desarrollador")
                            Thread.sleep(3000)
                            continue
                        } catch (e: IllegalArgumentException) {
                            providerAdded = true
                        } catch (e: Exception) {
                            notificar("ERROR: ${e.javaClass.simpleName}: ${e.message}")
                            Thread.sleep(1000)
                            continue
                        }
                    }

                    try { lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true) } catch (e: Exception) {}

                    val loc = MockHolder.location
                    if (loc == null) {
                        notificar("Esperando coordenadas… mueve el marcador en la app")
                    } else {
                        loc.provider = LocationManager.GPS_PROVIDER
                        try {
                            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)
                            notificar("OK: ${"%.5f".format(loc.latitude)}, ${"%.5f".format(loc.longitude)}")
                        } catch (e: Exception) {
                            notificar("ERROR al inyectar: ${e.message}")
                        }
                    }
                    Thread.sleep(1000)
                }
            }
            worker?.start()
        }
        return START_STICKY
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("mock", "Simulación GPS", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        running = false
        MockHolder.active = false
        try { MockHolder.lm?.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (e: Exception) {}
        MockHolder.lm = null
        super.onDestroy()
    }
}
