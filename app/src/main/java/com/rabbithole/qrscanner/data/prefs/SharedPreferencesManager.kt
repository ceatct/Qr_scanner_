package com.rabbithole.qrscanner.data.prefs

interface SharedPreferencesManager {
    fun saveElement(element: String)
    fun getAllElements(): List<String>
}