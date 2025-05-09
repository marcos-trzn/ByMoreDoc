package com.marcos.by_more_doc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class UploadActivity : AppCompatActivity() {


    private lateinit var editNombre: EditText
    private lateinit var spinnerCarpetas: Spinner
    private lateinit var editEtiquetas: EditText
    private lateinit var statusText: TextView

    private lateinit var btnGaleria: LinearLayout
    private lateinit var btnCamara: LinearLayout
    private lateinit var btnImportar: LinearLayout
    private val REQUEST_GALERIA = 1
    private val REQUEST_CAMARA = 2
    private val REQUEST_IMPORTAR = 3

    private var rutaTemporalGuardada: String? = null


    private lateinit var btnCancelar: Button
    private lateinit var btnSubir: Button
    private lateinit var backButton: ImageButton

    private fun obtenerCarpetas(directorio: File = filesDir, rutaBase: String = ""): List<String> {
        val rutas = mutableListOf<String>()
        val subCarpetas = directorio.listFiles()?.filter { it.isDirectory } ?: return rutas

        for (sub in subCarpetas) {
            val rutaRelativa = if (rutaBase.isEmpty()) sub.name else "$rutaBase/${sub.name}"
            rutas.add(rutaRelativa)
            rutas.addAll(obtenerCarpetas(sub, rutaRelativa))
        }

        return rutas.sorted()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)


        // Referencias
        editNombre = findViewById(R.id.editNombre)
        spinnerCarpetas = findViewById(R.id.spinnerCarpetas)
        editEtiquetas = findViewById(R.id.editEtiquetas)
        statusText = findViewById(R.id.statusText)

        btnGaleria = findViewById(R.id.btnGaleria)
        btnCamara = findViewById(R.id.btnCamara)
        btnImportar = findViewById(R.id.btnImportar)

        btnCancelar = findViewById(R.id.btnCancelar)
        btnSubir = findViewById(R.id.btnSubir)
        backButton = findViewById(R.id.backButton)

        // Spinner de carpetas
        val carpetasExistentes = obtenerCarpetas()
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, carpetasExistentes)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCarpetas.adapter = adaptador



        // Eventos
        btnCancelar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()

        }

        backButton.setOnClickListener {
            onBackPressed()
        }

        btnSubir.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val etiquetas = editEtiquetas.text.toString().trim()

            if (rutaTemporalGuardada == null || rutaTemporalGuardada!!.isEmpty()) {
                Toast.makeText(this, "Selecciona un archivo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Introduce un nombre para el documento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val archivoTemporal = File(rutaTemporalGuardada!!)
            if (!archivoTemporal.exists()) {
                Toast.makeText(this, "El archivo seleccionado ya no existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val carpetaRelativa = spinnerCarpetas.selectedItem?.toString() ?: "."
            val carpetaDestino = if (carpetaRelativa == ".") filesDir else File(filesDir, carpetaRelativa)
            if (!carpetaDestino.exists()) carpetaDestino.mkdirs()

            val archivoFinal = File(carpetaDestino, archivoTemporal.name)

            try {
                archivoTemporal.copyTo(archivoFinal, overwrite = true)
                archivoTemporal.delete()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                return@setOnClickListener
            }

            val entrada = EntradaDocumento(nombre, carpetaRelativa, etiquetas, archivoFinal.absolutePath)
            ArchivoJSONManager.guardarEntrada(this, entrada)

            Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }



        btnCamara.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CAMARA)
            }
        }
        btnGaleria.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/* video/*"
            startActivityForResult(intent, REQUEST_GALERIA)
        }

        btnImportar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_IMPORTAR)
        }



    }

    private fun guardarArchivoDesdeUri(uri: Uri, nombreArchivo: String): File? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, nombreArchivo)

            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun guardarBitmapComoArchivo(bitmap: Bitmap, nombreArchivo: String): File? {
        return try {
            val file = File(filesDir, "$nombreArchivo.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_GALERIA, REQUEST_IMPORTAR -> {
                    val uri = data.data
                    uri?.let {
                        val extension = contentResolver.getType(uri)?.substringAfterLast('/') ?: "bin"
                        val nombreArchivo = "archivo_${System.currentTimeMillis()}.$extension"
                        val archivo = guardarArchivoDesdeUri(it, nombreArchivo)
                        if (archivo != null) {
                            rutaTemporalGuardada = archivo.absolutePath // << AÑADIR ESTO
                            statusText.text = "Archivo cargado: ${archivo.name}"
                            Toast.makeText(this, "Guardado en: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                REQUEST_CAMARA -> {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val archivo = guardarBitmapComoArchivo(it, "foto_${System.currentTimeMillis()}")
                        if (archivo != null) {
                            rutaTemporalGuardada = archivo.absolutePath // << AÑADIR ESTO
                            statusText.text = "Foto guardada: ${archivo.name}"
                            Toast.makeText(this, "Foto guardada en: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

}
