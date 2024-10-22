package com.example.uts_map
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import org.w3c.dom.Attr

class Topbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,

    ): RelativeLayout(context, attrs, defStyleAttr) {
    private val titleTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.topbar, this, true)
        titleTextView = findViewById(R.id.Title)
    }

    fun setTitle(title: String) {
        titleTextView.text = title
    }
}