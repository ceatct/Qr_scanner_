package com.rabbithole.qrscanner.data.prefs

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManagerImpl(context: Context) : SharedPreferencesManager {
    private val PREFS_NAME = "MyPreferences"
    private val MAX_ELEMENTS = 5
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveElement(element: String) {
        val elements = getAllElements().toMutableList()
        if (elements.size >= MAX_ELEMENTS) {
            elements.removeAt(0)
        }
        elements.add(element)

        val editor = sharedPreferences.edit()
        editor.clear()
        elements.forEachIndexed { index, value ->
            editor.putString(index.toString(), value)
        }
        editor.apply()
    }

    override fun getAllElements(): List<String> {
        val elements = mutableListOf<String>()
        for (i in 0 until MAX_ELEMENTS) {
            val element = sharedPreferences.getString(i.toString(), null)
            element?.let {
                elements.add(element)
            }
        }
        return elements
    }
}