package com.marcos.by_more_doc

data class EntradaDocumento(
    var nombre: String,
    var carpeta: String,
    var etiquetas: String,
    var rutaArchivo: String,
    var favorito: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
    var contrasena: String? = null
)


