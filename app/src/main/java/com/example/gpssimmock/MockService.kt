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

/**
 * Servicio en primer plano que inyecta la última coordenada del simulador
 * como ubicación de prueba del proveedor GPS puro (LocationManager).
 * Requiere: app seleccionada como "app de ubicación ficticia" en Opciones
 * de desarrollador + permiso de ubicación.
 */
class MockService : Service() {

    private var running = false
    private var worker: Thread? = null
    private var providerAdded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        MockHolder.lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                while (running) {
                    val lm = MockHolder.lm ?: break

                    // 1) Registrar el proveedor de prueba (solo una vez)
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
                            Log.e("MockService",
                                "NO estás seleccionado como 'app de ubicación ficticia' en Opciones de desarrollador")
                            Thread.sleep(3000)
                            continue
                        } catch (e: IllegalArgumentException) {
                            providerAdded = true   // ya estaba agregado
                        } catch (e: Exception) {
                            Thread.sleep(1000)
                            continue
                        }
                    }

                    // 2) Marcarlo como habilitado y con posición disponible
                    try { lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true) } catch (e: Exception) {}
                    try { lm.setTestProviderStatus(
                            LocationManager.GPS_PROVIDER,
                            LocationManager.AVAILABLE, null, System.currentTimeMillis())
                    } catch (e: Exception) {}

                    // 3) Inyectar la ubicación actual del simulador
                    MockHolder.location?.let { loc ->
                        loc.provider = LocationManager.GPS_PROVIDER
                        try {
                            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)
                        } catch (e: Exception) {
                            Log.w("MockService", "setTestProviderLocation falló", e)
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
        try { MockHolder.lm?.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (e: Exception) {}
        MockHolder.lm = null
        super.onDestroy()
    }
}
