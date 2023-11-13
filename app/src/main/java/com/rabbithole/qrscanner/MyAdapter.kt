package com.rabbithole.qrscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private var elements: List<String>, private val context: Context) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textElement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_element, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val element = elements[position]
        holder.textView.text = element
        holder.itemView.setOnClickListener {
            handleItemClick(element)
        }
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    fun updateElements(newElements: List<String>) {
        elements = newElements
    }

    private fun handleItemClick(element: String) {
        if (isTextLink(element)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(element)
            context.startActivity(intent)
        } else {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", element)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    private fun isTextLink(input: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(input).matches()
    }
}
