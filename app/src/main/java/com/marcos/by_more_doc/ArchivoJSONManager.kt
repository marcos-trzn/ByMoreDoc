package com.marcos.by_more_doc

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

object ArchivoJSONManager {

    private const val NOMBRE_ARCHIVO = "entradas.json"

    private val gson = Gson()
    private val tipoLista: Type = object : TypeToken<MutableList<EntradaDocumento>>() {}.type


    fun eliminarEntrada(context: Context, rutaArchivo: String) {
        val lista = cargarEntradas(context).toMutableList()
        val nuevaLista = lista.filter { it.rutaArchivo != rutaArchivo }
        guardarLista(context, nuevaLista)
    }


    fun guardarEntrada(context: Context, entrada: EntradaDocumento): Boolean {
        val lista = cargarEntradas(context).toMutableList()
        val yaExiste = lista.any { it.rutaArchivo == entrada.rutaArchivo }

        return if (!yaExiste) {
            lista.add(entrada)
            guardarLista(context, lista)
            true
        } else {
            false
        }
    }



    fun cargarEntradas(context: Context): List<EntradaDocumento> {
        val archivo = File(context.filesDir, NOMBRE_ARCHIVO)
        if (!archivo.exists()) return emptyList()

        val contenido = archivo.readText()
        return try {
            gson.fromJson(contenido, tipoLista) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun guardarLista(context: Context, lista: List<EntradaDocumento>) {
        val archivo = File(context.filesDir, NOMBRE_ARCHIVO)
        archivo.writeText(gson.toJson(lista))
    }

    fun actualizarEntrada(context: Context, actualizada: EntradaDocumento, rutaAntigua: String? = null) {
        val lista = cargarEntradas(context).toMutableList()
        val rutaBuscada = rutaAntigua ?: actualizada.rutaArchivo
        val nuevaLista = lista.map {
            if (it.rutaArchivo == rutaBuscada) actualizada else it
        }
        guardarLista(context, nuevaLista)
    }


    fun sobrescribirEntradas(context: Context, entradas: List<EntradaDocumento>) {
        val archivo = File(context.filesDir, NOMBRE_ARCHIVO)
        if (archivo.exists()) archivo.delete() // solo borra este archivo, no todos los JSON

        // Guardar todos los documentos de forma segura
        val contenido = gson.toJson(entradas)
        archivo.writeText(contenido)
    }



}
