package com.rabbithole.qrscanner.repositories

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.rabbithole.qrscanner.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrRepositoryImpl : QrRepository {

    override fun creteQr(qrCodeContent: String, size: Int): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().also {
            it[EncodeHintType.MARGIN] = 1
            it[EncodeHintType.CHARACTER_SET] = "UTF-8"
        }
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size, hints)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    override fun saveImageToExternalStorage(context: Context, imageView: ImageView): Boolean {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val storageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QrScanner")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.png"

            val imageFile = File(storageDir, fileName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)

                val bitmap = (imageView.drawable as BitmapDrawable).bitmap

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                MediaScannerConnection.scanFile(context, arrayOf(imageFile.path), null, null)

                Toast.makeText(context, context.getText(R.string.saved), Toast.LENGTH_SHORT).show()

                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    override fun shareImageFromImageView(context: Context, imageView: ImageView) {
        val drawable = imageView.drawable
        if (drawable != null) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            try {
                val cachePath = File(context.externalCacheDir, "images")
                cachePath.mkdirs()
                val imageFile = File(cachePath, "image.png")

                val stream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.share)))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getText(R.string.save_error), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getText(R.string.no_img), Toast.LENGTH_SHORT).show()
        }
    }

}