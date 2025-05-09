package com.marcos.by_more_doc

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ExplorarCarpetaActivity : AppCompatActivity() {

    private lateinit var layoutContenido: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explorar_carpeta)

        layoutContenido = findViewById(R.id.layoutContenido)

        val nombreCarpeta = intent.getStringExtra("nombreCarpeta")
        if (nombreCarpeta == null) {
            finish()
            return
        }

        val carpeta = File(filesDir, nombreCarpeta)
        if (!carpeta.exists() || !carpeta.isDirectory) {
            finish()
            return
        }

        val archivos = carpeta.listFiles() ?: arrayOf()

        for (archivo in archivos) {
            val tv = TextView(this)
            tv.text = archivo.name
            tv.textSize = 16f
            layoutContenido.addView(tv)
        }
    }
}
