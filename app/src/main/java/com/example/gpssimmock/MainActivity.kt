package com.example.gpssimmock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var web: WebView

    /** Puente JS <-> Android: el simulador (HTML) nos manda cada coordenada. */
    inner class Bridge {
        @JavascriptInterface
        fun onLocation(lat: Double, lon: Double) {
            MockHolder.update(lat, lon)
        }

        @JavascriptInterface
        fun toast(msg: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true   // localStorage del simulador
        web.settings.allowFileAccess = true
        web.addJavascriptInterface(Bridge(), "Android")
        web.keepScreenOn = true
        setContentView(web)
        web.loadUrl("file:///android_asset/simulador.html")

        pedirPermisos()
    }

    private fun permisosOk(): Boolean {
        val f = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val c = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return f || c
    }

    private fun pedirPermisos() {
        val lista = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= 33) lista.add(Manifest.permission.POST_NOTIFICATIONS)
        requestPermissions(lista.toTypedArray(), 42)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permisosOk()) {
            iniciarServicio()
        } else {
            Toast.makeText(
                this,
                "Sin permiso de ubicación no puedo inyectar la ubicación ficticia",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun iniciarServicio() {
        val i = Intent(this, MockService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
    }
}
