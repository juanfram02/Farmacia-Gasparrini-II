# Farmacia Gasparrini II

La farmacia en botella, como app de Android y como fondo de pantalla animado.

## Instalar

1. Desde el celular, entrá a la pestaña **Releases** de este repositorio.
2. Bajá `farmacia-gasparrini-ii.apk` de la última versión y abrilo.
3. Si Android lo pide, permití instalar apps desde el navegador o el administrador de archivos.

Las versiones nuevas se instalan encima de la anterior: todas van firmadas con la misma clave (`app/farmacia.jks`).

## Qué trae

- **La farmacia completa**: hora, velocidad, clientes por hora, eventos, charlas y todo lo demás. Funciona sin internet.
- **Hora real u hora simulada**: con el interruptor «Hora real» la farmacia sigue el reloj del celular y abre y cierra en sus horarios de verdad.
- **Fondo de pantalla animado**: el botón «Usar de fondo de pantalla» la pone detrás de los íconos. Usa los mismos ajustes que elegiste en la app, gira un poco al pasar de pantalla y se detiene cuando no se ve, para cuidar la batería.

## Cómo se arma

Cada vez que se sube un cambio a `main`, GitHub Actions compila el APK (`.github/workflows/build.yml`) y lo publica en Releases.

- `app/src/main/assets/web/index.html`: la farmacia (la misma página de Claude, con three.js incluido).
- `MainActivity`: la app a pantalla completa.
- `FarmaciaWallpaperService`: el fondo de pantalla. Muestra la misma página en una pantalla virtual que dibuja sobre el fondo.
