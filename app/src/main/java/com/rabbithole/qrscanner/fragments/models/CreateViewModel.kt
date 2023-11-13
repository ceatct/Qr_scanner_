package com.rabbithole.qrscanner.fragments.models

import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rabbithole.qrscanner.data.QrData
import com.rabbithole.qrscanner.repositories.QrRepository

class CreateViewModel(private val repository: QrRepository) : ViewModel() {
    private val _qrData = MutableLiveData<QrData>()
    val qrData: LiveData<QrData> = _qrData

    fun createQrCode(text: String) {
        _qrData.value = QrData(text)
    }

    fun saveImageToExternalStorage(context: Context, imageView: ImageView) {
        repository.saveImageToExternalStorage(context, imageView)
    }

    fun shareImageFromImageView(context: Context, imageView: ImageView) {
        repository.shareImageFromImageView(context, imageView)
    }

}