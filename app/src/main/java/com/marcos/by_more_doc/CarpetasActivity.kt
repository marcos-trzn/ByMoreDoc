package com.marcos.by_more_doc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class CarpetasActivity : AppCompatActivity() {

    private lateinit var listaCarpetas: RecyclerView
    private lateinit var adaptador: CarpetaAdapter
    private lateinit var btnAgregarCarpeta: FloatingActionButton
    private lateinit var btnVolver: ImageButton
    private lateinit var titulo: TextView
    private lateinit var btnDocumentos: ImageButton

    private lateinit var rutaActual: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpetas)

        listaCarpetas = findViewById(R.id.listaCarpetas)
        btnAgregarCarpeta = findViewById(R.id.btnAgregarCarpeta)
        btnVolver = findViewById(R.id.btnVolver)
        titulo = findViewById(R.id.tituloCarpeta) // cambia el id del TextView si era diferente
        btnDocumentos = findViewById(R.id.btnDocumentos)



        // Obtener ruta actual o usar filesDir como raíz
        rutaActual = intent.getStringExtra("rutaCarpeta")?.let { File(it) } ?: filesDir

        titulo.text = rutaActual.absolutePath.removePrefix(filesDir.absolutePath).let {
            if (it.isEmpty()) "Carpetas" else it.replace(File.separatorChar, '/')
        }

        btnDocumentos.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)

        }


        listaCarpetas.layoutManager = LinearLayoutManager(this)
        adaptador = CarpetaAdapter(this, obtenerCarpetas(rutaActual), rutaActual)
        listaCarpetas.adapter = adaptador

        btnAgregarCarpeta.setOnClickListener {
            mostrarDialogoCrearCarpeta()
        }

        btnVolver.setOnClickListener {
            if (rutaActual != filesDir) {
                val intent = Intent(this, CarpetasActivity::class.java)
                intent.putExtra("rutaCarpeta", rutaActual.parent)
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }

    private fun obtenerCarpetas(ruta: File): MutableList<String> {
        return ruta.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }?.toMutableList() ?: mutableListOf()
    }

    private fun mostrarDialogoCrearCarpeta() {
        val input = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Nueva carpeta")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nueva = File(rutaActual, nombre)
                    if (nueva.exists()) {
                        Toast.makeText(this, "La carpeta ya existe", Toast.LENGTH_SHORT).show()
                    } else if (nueva.mkdirs()) {
                        adaptador.carpetas.add(nombre)
                        adaptador.notifyItemInserted(adaptador.carpetas.size - 1)
                    } else {
                        Toast.makeText(this, "No se pudo crear la carpeta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    class CarpetaAdapter(
        private val context: Context,
        val carpetas: MutableList<String>,
        val rutaActual: File
    ) : RecyclerView.Adapter<CarpetaAdapter.ViewHolder>() {


        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icono: ImageView = view.findViewById(R.id.iconocarpeta)
            val nombre: TextView = view.findViewById(R.id.nombrecarpeta)
            val opciones: ImageButton = view.findViewById(R.id.btnopciones)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_carpeta, parent, false)
            return ViewHolder(vista)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val carpeta = carpetas[position]
            holder.nombre.text = carpeta

            // Abrir la carpeta al hacer click
            holder.itemView.setOnClickListener {
                val carpetaFile = File(rutaActual, carpeta)
                val intent = Intent(context, CarpetasActivity::class.java)
                intent.putExtra("rutaCarpeta", carpetaFile.absolutePath)
                context.startActivity(intent)
            }

            // Menú de opciones
            holder.opciones.setOnClickListener {
                val menu = PopupMenu(context, holder.opciones)
                menu.menuInflater.inflate(R.menu.menu_carpeta, menu.menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_renombrar -> {
                            mostrarDialogoRenombrar(context, carpeta, position)
                            true
                        }
                        R.id.menu_eliminar -> {
                            AlertDialog.Builder(context)
                                .setTitle("¿Eliminar carpeta?")
                                .setMessage("Se borrará la carpeta \"$carpeta\" y todo su contenido. ¿Está seguro?")
                                .setPositiveButton("Sí") { _, _ ->
                                    val file = File(rutaActual, carpeta)
                                    if (file.deleteRecursively()) {
                                        carpetas.removeAt(position)
                                        notifyItemRemoved(position)
                                        Toast.makeText(context, "Carpeta eliminada", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("No", null)
                                .show()
                            true
                        }
                        R.id.menu_mover -> {
                            mostrarDialogoMover(context, carpeta, position)
                            true
                        }

                        else -> false
                    }
                }
                menu.show()
            }
        }

        override fun getItemCount() = carpetas.size

        private fun mostrarDialogoRenombrar(context: Context, carpeta: String, position: Int) {
            val input = EditText(context).apply {
                setText(carpeta)
            }

            AlertDialog.Builder(context)
                .setTitle("Renombrar carpeta")
                .setView(input)
                .setPositiveButton("Guardar") { _, _ ->
                    val nuevoNombre = input.text.toString().trim()
                    if (nuevoNombre.isNotEmpty()) {
                        val origen = File(rutaActual, carpeta)
                        val destino = File(rutaActual, nuevoNombre)
                        if (origen.renameTo(destino)) {
                            carpetas[position] = nuevoNombre
                            notifyItemChanged(position)
                        } else {
                            Toast.makeText(context, "No se pudo renombrar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        private fun mostrarDialogoMover(context: Context, carpeta: String, position: Int) {
            val origen = File(rutaActual, carpeta)
            val carpetasDisponibles = obtenerRutasRecursivas(context.filesDir, "", origen)
                .sortedWith(compareBy { if (it == ".") "~" else it }) // "." queda al final

            val spinner = Spinner(context)
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, carpetasDisponibles)
            spinner.adapter = adapter

            AlertDialog.Builder(context)
                .setTitle("Mover carpeta")
                .setMessage("Selecciona la carpeta de destino:")
                .setView(spinner)
                .setPositiveButton("Mover") { _, _ ->
                    val rutaSeleccionada = spinner.selectedItem.toString()
                    val destino = if (rutaSeleccionada == ".") context.filesDir else File(context.filesDir, rutaSeleccionada)

                    if (!destino.exists()) destino.mkdirs()

                    val nuevaRuta = File(destino, carpeta)
                    if (origen.renameTo(nuevaRuta)) {
                        carpetas.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Carpeta movida", Toast.LENGTH_SHORT).show()

                        // Actualiza las rutas en los documentos guardados para que no se pierda
                        val todasLasEntradas = ArchivoJSONManager.cargarEntradas(context)
                        val entradasActualizadas = todasLasEntradas.map { entrada ->
                            if (entrada.rutaArchivo.startsWith(origen.absolutePath)) {
                                val nuevaRutaArchivo = entrada.rutaArchivo.replaceFirst(origen.absolutePath, nuevaRuta.absolutePath)
                                entrada.copy(rutaArchivo = nuevaRutaArchivo)
                            } else {
                                entrada
                            }
                        }

                        // Reescribe los archivos JSON con las rutas nuevas
                        ArchivoJSONManager.sobrescribirEntradas(context, entradasActualizadas)

                    } else {
                        Toast.makeText(context, "No se pudo mover", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }



        private fun obtenerRutasRecursivas(directorio: File, rutaBase: String = "", carpetaAExcluir: File): List<String> {
            val rutas = mutableListOf<String>()

            // Solo añadir "." si estamos en la raíz de la llamada
            if (rutaBase.isEmpty()) {
                rutas.add(".") // Representa la raíz
            }

            val subCarpetas = directorio.listFiles()?.filter { it.isDirectory } ?: return rutas

            for (sub in subCarpetas) {
                // Evita incluir la carpeta a excluir ni sus hijos
                if (sub.absolutePath.startsWith(carpetaAExcluir.absolutePath)) continue

                val rutaRelativa = if (rutaBase.isEmpty()) sub.name else "$rutaBase/${sub.name}"
                rutas.add(rutaRelativa)
                rutas.addAll(obtenerRutasRecursivas(sub, rutaRelativa, carpetaAExcluir))
            }

            return rutas
        }

    }

}
