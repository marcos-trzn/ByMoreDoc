    package com.marcos.by_more_doc

    import android.graphics.BitmapFactory
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.text.InputType
    import android.widget.*
    import androidx.annotation.RequiresExtension
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.fragment.app.FragmentContainerView
    import com.marcos.by_more_doc.renderPdfPages




    import java.io.File

    class VistaDocumentoActivity : AppCompatActivity() {

        private lateinit var tituloDocumento: TextView
        private var archivo: File? = null
        private var rutaArchivo: String? = null
        private var nombrePersonalizado: String? = null

        private var entradaActual: EntradaDocumento? = null
        private lateinit var btnVolver: ImageButton
        private lateinit var btnInfo: ImageButton
        private lateinit var btnEditar: ImageButton
        private lateinit var btnFavorito: ImageButton
        private lateinit var btnAlerta: ImageButton
        private lateinit var btnEliminar: ImageButton

        @RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Obtener datos del Intent
            rutaArchivo = intent.getStringExtra("rutaArchivo")
            nombrePersonalizado = intent.getStringExtra("nombrePersonalizado") ?: "Documento"
            archivo = rutaArchivo?.let { File(it) }

            val extension = archivo?.extension?.lowercase()

            // Establecer layout según el tipo de archivo
            when (extension) {
                "jpg", "jpeg", "png" -> setContentView(R.layout.activity_vista_imagen)
                "pdf" -> setContentView(R.layout.activity_vista_pdf)
                "mp4", "avi", "mov" -> setContentView(R.layout.activity_vista_video)
                "txt", "md" -> setContentView(R.layout.activity_vista_texto)
                else -> setContentView(R.layout.activity_detalles_documento)
            }


            // Referencias comunes
            tituloDocumento = findViewById(R.id.tituloDocumento)
            btnVolver = findViewById(R.id.btnVolver)
            btnInfo = findViewById(R.id.btnInfo)
            btnEditar = findViewById(R.id.btnEditar)
            btnFavorito = findViewById(R.id.btnFavorito)
            btnAlerta = findViewById(R.id.btnAlerta)
            btnEliminar = findViewById(R.id.btnEliminar)

            val todas = ArchivoJSONManager.cargarEntradas(this)
            entradaActual = todas.find { it.rutaArchivo == rutaArchivo }
            actualizarIconoFavorito()
            tituloDocumento.text = nombrePersonalizado

            // Mostrar contenido según el tipo de archivo
            when (extension) {
                "jpg", "jpeg", "png" -> {
                    val imageView = findViewById<ImageView>(R.id.imageView)
                    val bitmap = BitmapFactory.decodeFile(archivo!!.absolutePath)
                    imageView.setImageBitmap(bitmap)
                }


                "pdf" -> {
                    val container = findViewById<LinearLayout>(R.id.pdfContainer)
                    archivo?.let {
                        renderPdfPages(it, container)
                    }
                }





                "mp4", "avi", "mov" -> {
                    val videoView = findViewById<VideoView>(R.id.videoView)
                    archivo?.let {
                        val mediaController = MediaController(this)
                        mediaController.setAnchorView(videoView)
                        mediaController.setMediaPlayer(videoView)

                        videoView.setMediaController(mediaController)
                        videoView.setVideoPath(it.absolutePath)
                        videoView.requestFocus()
                        videoView.start()
                    }
                }


                "txt", "md" -> {
                    val textView = findViewById<TextView>(R.id.textViewContenido)
                    archivo?.let {
                        textView.text = it.readText()
                    }
                }
            }

            // Botón volver
            btnVolver.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            // Botón favorito
            btnFavorito.setOnClickListener {
                entradaActual?.let {
                    it.favorito = !it.favorito
                    ArchivoJSONManager.actualizarEntrada(this, it)
                    actualizarIconoFavorito()
                }
            }

            // Botón eliminar
            btnEliminar.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("¿Seguro que quiere borrarlo?")
                    .setPositiveButton("Sí") { _, _ ->
                        archivo?.let {
                            if (it.exists() && it.delete()) {
                                ArchivoJSONManager.eliminarEntrada(this, rutaArchivo ?: "")
                                Toast.makeText(this, "Archivo y datos eliminados", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            // Botón editar
            btnEditar.setOnClickListener {
                entradaActual?.let { entrada ->
                    val layout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(50, 40, 50, 10)
                    }

                    val inputNombre = EditText(this).apply {
                        hint = "Nuevo nombre"
                        setText(entrada.nombre)
                    }

                    val inputEtiquetas = EditText(this).apply {
                        hint = "Nuevas etiquetas"
                        setText(entrada.etiquetas)
                    }

                    // Carpetas disponibles (reutilizando la lógica de CarpetasActivity)
                    val carpetasDisponibles = obtenerRutasRecursivas(filesDir, "", File(entrada.rutaArchivo).parentFile)
                        .sortedWith(compareBy { if (it == ".") "~" else it })

                    val spinner = Spinner(this)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carpetasDisponibles)
                    spinner.adapter = adapter

                    layout.addView(inputNombre)
                    layout.addView(inputEtiquetas)
                    layout.addView(TextView(this).apply { text = "Carpeta destino:" })
                    layout.addView(spinner)

                    AlertDialog.Builder(this)
                        .setTitle("Editar documento")
                        .setView(layout)
                        .setPositiveButton("Guardar") { _, _ ->
                            val nuevoNombre = inputNombre.text.toString().trim()
                            val nuevasEtiquetas = inputEtiquetas.text.toString().trim()
                            val rutaSeleccionada = spinner.selectedItem.toString()

                            if (nuevoNombre.isNotBlank()) {
                                val nuevaRuta = if (rutaSeleccionada == ".") filesDir else File(filesDir, rutaSeleccionada)
                                val archivoViejo = File(entrada.rutaArchivo)
                                val archivoNuevo = File(nuevaRuta, archivoViejo.name)

                                if (archivoViejo.absolutePath != archivoNuevo.absolutePath) {
                                    if (!nuevaRuta.exists()) nuevaRuta.mkdirs()
                                    archivoViejo.copyTo(archivoNuevo, overwrite = true)
                                    archivoViejo.delete()
                                }

                                entrada.nombre = nuevoNombre
                                entrada.etiquetas = nuevasEtiquetas
                                entrada.timestamp = System.currentTimeMillis()
                                entrada.rutaArchivo = archivoNuevo.absolutePath

                                archivo = archivoNuevo
                                rutaArchivo = archivoNuevo.absolutePath

                                ArchivoJSONManager.actualizarEntrada(this, entrada, rutaAntigua = rutaArchivo)
                                tituloDocumento.text = nuevoNombre
                                Toast.makeText(this, "Documento actualizado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }


            // Botón alerta
            btnAlerta.setOnClickListener {
                entradaActual?.let { entrada ->
                    val input = EditText(this).apply {
                        hint = "Escribe la contraseña"
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    val layout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(40, 30, 40, 10)
                        addView(input)
                    }

                    val tieneContraseña = !entrada.contrasena.isNullOrEmpty()

                    AlertDialog.Builder(this)
                        .setTitle(if (tieneContraseña) "Actualizar contraseña" else "Establecer contraseña")
                        .setView(layout)
                        .setPositiveButton("Guardar") { _, _ ->
                            val nueva = input.text.toString().trim()
                            entrada.contrasena = if (nueva.isNotEmpty()) nueva else null
                            ArchivoJSONManager.actualizarEntrada(this, entrada)
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }

            // Botón info
            btnInfo.setOnClickListener {
                archivo?.let {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_info_documento, null)

                    val nombre = entradaActual?.nombre ?: it.name
                    val tamañoMB = it.length() / (1024.0 * 1024.0)
                    val fecha = android.text.format.DateFormat.format("dd MMM yyyy hh:mm a", it.lastModified())
                    val ubicacion = it.absolutePath
                        .removePrefix(filesDir.absolutePath)
                        .removePrefix("/")
                        .replace(File.separator, " > ")

                    val extension = it.extension.lowercase()
                    var resolucion = "Desconocida"

                    when (extension) {
                        "jpg", "jpeg", "png" -> {
                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            resolucion = "${bitmap.width} x ${bitmap.height}"
                        }
                        "mp4", "avi", "mov" -> {
                            val retriever = android.media.MediaMetadataRetriever()
                            retriever.setDataSource(it.absolutePath)
                            val width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                            val height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                            resolucion = if (width != null && height != null) "$width x $height" else "Desconocida"
                            retriever.release()
                        }
                        "pdf" -> {
                            resolucion = "PDF (páginas múltiples)"
                        }
                        "txt", "md" -> {
                            resolucion = "Archivo de texto plano"
                        }
                    }

                    dialogView.findViewById<TextView>(R.id.info_nombre).text = "Nombre\n$nombre"
                    dialogView.findViewById<TextView>(R.id.info_tamano).text = "Tamaño\n${"%.1f".format(tamañoMB)} MB"
                    dialogView.findViewById<TextView>(R.id.info_fecha).text = "Modificado\n$fecha"
                    dialogView.findViewById<TextView>(R.id.info_resolucion).text = "Resolución\n$resolucion"
                    dialogView.findViewById<TextView>(R.id.info_ubicacion).text = "Ubicación\nAlmacenamiento interno > $ubicacion"

                    AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
            }

        }

        private fun actualizarIconoFavorito() {
            val icono = if (entradaActual?.favorito == true)
                R.drawable.ic_star_filled
            else
                R.drawable.ic_star_border

            btnFavorito.setImageResource(icono)
        }
        private fun obtenerRutasRecursivas(
            directorio: File,
            rutaBase: String = "",
            carpetaAExcluir: File?
        ): List<String> {
            val rutas = mutableListOf<String>()
            if (rutaBase.isEmpty()) rutas.add(".")

            val subCarpetas = directorio.listFiles()?.filter { it.isDirectory } ?: return rutas

            for (sub in subCarpetas) {
                val rutaRelativa = if (rutaBase.isEmpty()) sub.name else "$rutaBase/${sub.name}"

                // Solo evitar añadir la carpeta exacta, pero seguir con sus subcarpetas
                if (carpetaAExcluir != null && sub.absolutePath == carpetaAExcluir.absolutePath) {
                    rutas.addAll(obtenerRutasRecursivas(sub, rutaRelativa, null)) // permitir sus hijas
                } else {
                    rutas.add(rutaRelativa)
                    rutas.addAll(obtenerRutasRecursivas(sub, rutaRelativa, carpetaAExcluir))
                }
            }

            return rutas
        }



    }


