package com.marcos.by_more_doc

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.setPadding
import java.io.File

fun renderPdfPages(file: File, container: LinearLayout) {
    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(fileDescriptor)

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        val imageView = ImageView(container.context).apply {
            setImageBitmap(bitmap)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            setPadding(4)
        }
        container.addView(imageView)
        page.close()
    }

    renderer.close()
    fileDescriptor.close()
}
