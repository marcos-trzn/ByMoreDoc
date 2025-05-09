package com.marcos.by_more_doc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val launcherUpload =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarEntradasDesdeJson()
                filtrarYMostrar()
            }
        }

    private lateinit var listContainer: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var filtroFavoritosButton: ImageButton
    private lateinit var orderButton: ImageButton
    private lateinit var btnDocumentos: ImageButton
    private lateinit var btnCarpetas: ImageButton
    private lateinit var navigateButton: FloatingActionButton

    private val entradasOriginales = mutableListOf<EntradaDocumento>()
    private var mostrarSoloFavoritos = false
    private var ordenAscendente = true

    override fun onCreate(savedInstanceState: Bundle?) {

        // BORRAR TODOS LOS ARCHIVOS JSON PARA PRUEBAS
        //val dir = filesDir
        //dir.listFiles()?.forEach { file -> if (file.extension == "json") { file.delete() } }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listContainer = findViewById(R.id.listContainer)
        searchEditText = findViewById(R.id.searchEditText)
        filtroFavoritosButton = findViewById(R.id.filtroFavoritosButton)
        orderButton = findViewById(R.id.orderButton)
        btnDocumentos = findViewById(R.id.btnDocumentos)
        btnCarpetas = findViewById(R.id.btnCarpetas)
        navigateButton = findViewById(R.id.navigateButton)

        navigateButton.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            launcherUpload.launch(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrarYMostrar()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        filtroFavoritosButton.setOnClickListener {
            mostrarSoloFavoritos = !mostrarSoloFavoritos
            filtroFavoritosButton.setImageResource(
                if (mostrarSoloFavoritos) R.drawable.ic_star_filled else R.drawable.ic_star_border
            )
            filtrarYMostrar()
        }

        orderButton.setOnClickListener {
            val menuPopup = PopupMenu(this, orderButton)
            menuPopup.menu.add("Ordenar A-Z")
            menuPopup.menu.add("Ordenar Z-A")
            menuPopup.menu.add("Buscar por etiqueta")

            menuPopup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Ordenar A-Z" -> {
                        ordenAscendente = true
                        filtrarYMostrar()
                        true
                    }
                    "Ordenar Z-A" -> {
                        ordenAscendente = false
                        filtrarYMostrar()
                        true
                    }
                    "Buscar por etiqueta" -> {
                        mostrarDialogoEtiqueta()
                        true
                    }
                    else -> false
                }
            }

            menuPopup.show()
        }


        btnCarpetas.setOnClickListener {
            val intent = Intent(this, CarpetasActivity::class.java)
            startActivity(intent)
        }

        btnDocumentos.setOnClickListener { filtrarYMostrar() }

        cargarEntradasDesdeJson()
        filtrarYMostrar()
    }

    private fun cargarEntradasDesdeJson() {
        entradasOriginales.clear()
        entradasOriginales.addAll(ArchivoJSONManager.cargarEntradas(this))
    }

    private fun filtrarYMostrar() {
        val texto = searchEditText.text.toString().lowercase()
        val listaFiltrada = entradasOriginales
            .filter {
                (!mostrarSoloFavoritos || it.favorito) &&
                        it.nombre.lowercase().contains(texto)
            }

            .let {
                if (ordenAscendente) it.sortedBy { doc -> doc.nombre.lowercase() }
                else it.sortedByDescending { doc -> doc.nombre.lowercase() }
            }

        mostrarLista(listaFiltrada)
    }

    private fun mostrarLista(lista: List<EntradaDocumento>) {
        listContainer.removeAllViews()

        for (doc in lista) {
            val item = layoutInflater.inflate(R.layout.item_documento, listContainer, false)

            val tvNombre = item.findViewById<TextView>(R.id.tvNombreDocumento)
            val tvFecha = item.findViewById<TextView>(R.id.tvFechaDocumento)
            val tvTam = item.findViewById<TextView>(R.id.tvTamanioDocumento)
            val ivIcono = item.findViewById<ImageView>(R.id.ivIconoTipo)

            tvNombre.text = doc.nombre

            val fecha = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(doc.timestamp))
            tvFecha.text = "Modificado $fecha"

            val archivo = File(doc.rutaArchivo)
            val sizeKb = archivo.length() / 1024.0
            val tamano = if (sizeKb > 1024) "${"%.1f".format(sizeKb / 1024)}MB" else "${"%.1f".format(sizeKb)}KB"
            tvTam.text = tamano

            val extension = archivo.extension.lowercase()
            val icono = when (extension) {
                "pdf" -> R.drawable.ic_pdf
                "jpg", "jpeg", "png" -> R.drawable.ic_image
                "mp4", "avi" -> R.drawable.ic_video
                "txt" -> R.drawable.ic_text
                else -> R.drawable.ic_file_generic
            }
            ivIcono.setImageResource(icono)

            item.setOnClickListener {
                if (!doc.contrasena.isNullOrEmpty()) {
                    val input = EditText(this)
                    input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

                    AlertDialog.Builder(this)
                        .setTitle("Protegido con contraseña")
                        .setMessage("Introduce la contraseña para acceder a \"${doc.nombre}\"")
                        .setView(input)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar") { _, _ ->
                            if (input.text.toString() == doc.contrasena) {
                                abrirVistaDocumento(doc)
                            } else {
                                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    abrirVistaDocumento(doc)
                }
            }

            listContainer.addView(item)
        }

    }
    private fun abrirVistaDocumento(doc: EntradaDocumento) {
        val intent = Intent(this, VistaDocumentoActivity::class.java).apply {
            putExtra("rutaArchivo", doc.rutaArchivo)
            putExtra("nombrePersonalizado", doc.nombre)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEtiqueta() {
        val etiquetasUnicas = entradasOriginales
            .flatMap { it.etiquetas.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        if (etiquetasUnicas.isEmpty()) {
            Toast.makeText(this, "No hay etiquetas disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val spinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etiquetasUnicas)
        spinner.adapter = adapter

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
            addView(TextView(this@MainActivity).apply { text = "Selecciona una etiqueta:" })
            addView(spinner)
        }

        AlertDialog.Builder(this)
            .setTitle("Buscar por etiqueta")
            .setView(layout)
            .setPositiveButton("Buscar") { _, _ ->
                val seleccionada = spinner.selectedItem.toString().lowercase()
                val filtrados = entradasOriginales.filter {
                    it.etiquetas.lowercase().split(",").map { e -> e.trim() }.contains(seleccionada)
                }
                mostrarLista(filtrados)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    override fun onResume() {
        super.onResume()
        cargarEntradasDesdeJson()
        filtrarYMostrar()
    }

}


