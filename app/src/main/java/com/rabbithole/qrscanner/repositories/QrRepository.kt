package com.rabbithole.qrscanner.repositories

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView

interface QrRepository {
    fun creteQr(qrCodeContent: String, size: Int):Bitmap
    fun saveImageToExternalStorage(context: Context, imageView: ImageView): Boolean
    fun shareImageFromImageView(context: Context, imageView: ImageView)
}