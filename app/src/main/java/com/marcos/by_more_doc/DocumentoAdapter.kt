package com.marcos.by_more_doc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DocumentoAdapter(
    private val lista: List<EntradaDocumento>
) : RecyclerView.Adapter<DocumentoAdapter.DocumentoViewHolder>() {

    class DocumentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icono: ImageView = view.findViewById(R.id.ivIconoTipo)
        val nombre: TextView = view.findViewById(R.id.tvNombreDocumento)
        val fecha: TextView = view.findViewById(R.id.tvFechaDocumento)
        val tamanio: TextView = view.findViewById(R.id.tvTamanioDocumento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_documento, parent, false)
        return DocumentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentoViewHolder, position: Int) {
        val documento = lista[position]

        holder.nombre.text = documento.nombre

        // Fecha formateada desde timestamp
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.fecha.text = "Modificado ${sdf.format(Date(documento.timestamp))}"

        // Tamaño (si se desea obtener del archivo físico)
        val archivo = java.io.File(documento.rutaArchivo)
        if (archivo.exists()) {
            val sizeInMB = archivo.length().toDouble() / (1024 * 1024)
            holder.tamanio.text = String.format(Locale.getDefault(), "%.1fMB", sizeInMB)
        } else {
            holder.tamanio.text = "Desconocido"
        }

        // Icono (puedes mejorar esto con lógica si quieres diferentes íconos por tipo)
        holder.icono.setImageResource(R.drawable.ic_pdf)
    }

    override fun getItemCount(): Int = lista.size
}
