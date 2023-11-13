package com.rabbithole.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rabbithole.qrscanner.fragments.models.CreateViewModel
import com.rabbithole.qrscanner.repositories.QrRepository

class CreateViewModelFactory(private val repository: QrRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateViewModel::class.java)) {
            return CreateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
