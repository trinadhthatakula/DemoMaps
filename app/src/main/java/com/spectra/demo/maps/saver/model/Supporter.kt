package com.spectra.demo.maps.saver.model

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.spectra.demo.maps.saver.BuildConfig
import org.koin.core.annotation.Single
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


@Single
class Supporter {

    var polyData: PolyData = PolyData()
    val markedPoints: ArrayList<LatLng> = ArrayList()
    val tripPoints: ArrayList<LatLng> = ArrayList()
    var mapSnap: Bitmap? = null
    var polyMode = PolyMode.GON

    fun getColor(resId: Int, mContext: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray = mContext.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun saveToFile(text: String, file: File): Boolean {
        return try {
            file.writeText(text)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        // Save bitmap to local storage
        val imagePath = saveBitmapToInternalStorage(context, bitmap, title)

        // Create intent to share the bitmap
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        val uri = Uri.fromFile(File(imagePath))
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, title: String): String {
        // Save bitmap to a file in internal storage
        val imagesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDirectory, "$title.jpg")
        val outputStream: OutputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Trigger media scanner to scan the saved image file
        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.path),
            null,
            null
        )

        return imageFile.path
    }

    fun shareFile(file: File, context: Context) {
        val uri =
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "*/*" // Change the MIME type as needed
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

}

fun Uri.copyToFile(file: File, context: Context): Boolean {
    return try {
        context.contentResolver.openInputStream(this).use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun File.readFileText() = try {
    readText()
} catch (e: Exception) {
    e.printStackTrace()
    ""
}


fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable!!.setBounds(0, 0, (vectorDrawable.intrinsicWidth),
        (vectorDrawable.intrinsicHeight)
    )
    val bitmap = Bitmap.createBitmap(
        (vectorDrawable.intrinsicWidth),
        (vectorDrawable.intrinsicHeight),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

