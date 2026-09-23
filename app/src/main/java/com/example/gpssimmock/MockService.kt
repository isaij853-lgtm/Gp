package com.example.gpssimmock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.LocationServices

/**
 * Servicio en primer plano que inyecta la última coordenada del simulador
 * como ubicación ficticia del sistema (requiere ser la app de ubicación
 * ficticia seleccionada en Opciones de desarrollador).
 */
class MockService : Service() {

    private var running = false
    private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        MockHolder.fused = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()
        val n = Notification.Builder(this, "mock")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("GPS Simulador")
            .setContentText("Ubicación ficticia activa")
            .setOngoing(true)
            .build()
        startForeground(1, n)

        if (!running) {
            running = true
            MockHolder.active = true
            worker = Thread {
                var mockMode = false
                while (running) {
                    if (!mockMode) {
                        try {
                            MockHolder.fused?.setMockMode(true)
                            mockMode = true
                            Log.i("MockService", "setMockMode(true) OK")
                        } catch (se: SecurityException) {
                            Log.e("MockService",
                                "Esta app NO está seleccionada como 'app de ubicación ficticia' en Opciones de desarrollador")
                            Thread.sleep(3000)
                            continue
                        } catch (e: Exception) {
                            Thread.sleep(1000)
                            continue
                        }
                    }
                    MockHolder.location?.let { loc ->
                        try {
                            MockHolder.fused?.setMockLocation(loc)
                        } catch (e: Exception) {
                            Log.w("MockService", "setMockLocation falló", e)
                        }
                    }
                    Thread.sleep(500)
                }
            }
            worker?.start()
        }
        return START_STICKY
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("mock", "Simulación GPS", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        running = false
        MockHolder.active = false
        try { MockHolder.fused?.setMockMode(false) } catch (e: Exception) { }
        super.onDestroy()
    }
}
