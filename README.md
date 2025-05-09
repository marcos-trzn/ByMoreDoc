# 📄 ByMoreDoc

**ByMoreDoc** es una aplicación Android que permite gestionar documentos locales y en la nube de manera eficiente y segura. Diseñada para ofrecer una experiencia intuitiva, esta app permite subir, visualizar, organizar y proteger archivos en distintos formatos, todo desde tu dispositivo móvil.

---

## ✨ Características principales

- 📤 **Subida de documentos** desde galería, cámara o archivos del dispositivo.
- 🗂️ **Organización por carpetas** personalizables.
- 🔖 **Etiquetado inteligente** para búsqueda rápida por palabras clave.
- 🔍 **Buscador avanzado** por nombre, fecha o etiquetas.
- ⭐ **Marcado de favoritos** para acceso rápido.
- 🧾 **Visualización de PDFs, imágenes, videos y textos**.
- 🔒 **Protección por contraseña** en documentos sensibles.
- 🧠 **Metadatos detallados** (tamaño, fecha, ubicación, resolución).
- 🔁 **Renombrar, mover o eliminar documentos y carpetas**.
- 
---

## 🚀 Instalación

### ✅ Requisitos

- Android 5.0 (API 21) o superior  
- Espacio de almacenamiento local

### 📦 Instalación manual

1. Descarga el archivo `.apk` desde la [sección de releases](https://github.com/marcos-trzn/ByMoreDoc/releases) o desde el repositorio.
2. Habilita “**Orígenes desconocidos**” en tu dispositivo.
3. Instala el archivo APK y abre la aplicación.

---

## 🔧 Configuración

- Al iniciar por primera vez no requiere configuración.
- Los documentos se almacenan en el directorio privado de la app.
  
---

## 🧪 Pruebas realizadas

- Subida de archivos desde diferentes orígenes (galería, cámara, archivos).
- Visualización de PDFs, imágenes, vídeos y textos sin errores.
- Funcionalidad de favoritos, edición, protección por contraseña y eliminación de documentos.
- Medición de rendimiento (bajo consumo de RAM y CPU).
- Funcionalidad sin conexión verificada.
- Compatibilidad con lectores de pantalla para accesibilidad.

---

## 🏗️ Estructura del proyecto

- `MainActivity`: Vista principal con buscador y filtros.
- `UploadActivity`: Permite subir y guardar documentos.
- `VistaDocumentoActivity`: Muestra el documento y permite gestionarlo.
- `ArchivoJSONManager`: Controlador de almacenamiento local en JSON.
- `CarpetasActivity`: Gestión de carpetas personalizadas.

---

## 🔐 Privacidad y seguridad

- Todos los documentos están protegidos en el almacenamiento privado (`filesDir`).
- Opción de contraseña por archivo.

---

## 🛠️ Tecnologías utilizadas

- Kotlin
- PDF Viewer
- Android Jetpack
- GSON

---

## 📁 APK y documentación

- 📥 [Última versión APK](https://github.com/marcos-trzn/ByMoreDoc/releases)
- 🧾 Memoria del proyecto incluida en `/docs`
- 📦 Incluye plan de pruebas, requisitos, y guía de instalación

---

## 📌 Autor

Desarrollado por **Marcos Lasheras Aleluia**  
Repositorio: [github.com/marcos-trzn/ByMoreDoc](https://github.com/marcos-trzn/ByMoreDoc)

---
