# GPS Simulador con Ubicación Ficticia (Android)

Tu simulador de rutas (Tren Buenavista→AIFA, micros) corriendo DENTRO de una app
Android que inyecta las coordenadas simuladas como ubicación ficticia del sistema.

## 1. Instalar la app en tu Samsung
1. Abre esta carpeta en **Android Studio** (abre `settings.gradle`).
2. Deja que sincronice Gradle (si pide actualizar el Gradle wrapper o el AGP, acepta).
3. Conecta tu Samsung por USB con **Depuración USB** activada y pulsa **Run ▶**,
   o genera el APK con **Build > Build APK(s)** y pásalo al teléfono.
4. Abre la app **GPS Simulador** en el teléfono y concede los permisos que pide
   (Ubicación + Notificaciones).

## 2. Activar la ubicación ficticia en Samsung (One UI)
1. Ajustes > Acerca del teléfono > Información de software >
   toca **Número de compilación** 7 veces (ya lo tienes si ves Opciones de desarrollador).
2. Ajustes > **Opciones de desarrollador** > **Seleccionar aplicación de ubicación ficticia**
   (en inglés: *Select mock location app*) > elige **GPS Simulador**.

## 3. Simular
1. Abre la app, elige la ruta, velocidad (60× o 300×) y pulsa **▶ Iniciar**.
2. La coordenada que ves en pantalla se inyecta al sistema cada 0.5 s con
   velocidad y rumbo calculados. Abre Google Maps o la app que estés probando:
   verá la ubicación del "tren" o de la "micro" moviéndose.

## Notas
- La app queda con una notificación fija mientras el servicio corre. Al cerrarla
  (o pausar la simulación) la ubicación real vuelve sola.
- Mantén la pantalla encendida: la app ya lo hace, porque si apagas la pantalla
  el navegador pausa la animación y la coordenada deja de avanzar.
- Si otra app sigue viendo tu ubicación real: verifica el paso 2 de arriba y que
  el **GPS del teléfono esté encendido**; desactiva "Mejorar precisión" en
  Ajustes > Ubicación si interfiere.
- El botón **Exportar** del simulador sigue guardando tus rutas en JSON.

## Alternativa: compilar el APK en la nube (sin Android Studio)
1. Crea una cuenta gratis en https://github.com y un repositorio nuevo (público o privado).
2. Sube a ese repo TODOS los archivos de esta carpeta (incluida la carpeta oculta `.github`).
3. Ve a la pestaña **Actions** del repo: el workflow "Build APK" se ejecuta solo.
4. Espera a que termine (luz verde), abre esa corrida y en la seccion **Artifacts**
   descarga `app-debug-apk`. Descomprime y obtienes `app-debug.apk`.
5. Pásalo a tu Samsung, ábrelo e instálalo (permite "instalar apps de esta fuente").
