package com.ecreditpal.danaflash.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ecreditpal.danaflash.R

@SuppressLint("Recycle")
class SettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var startIconView: ImageView;
    private var titleView: TextView
    private var subTitleView: TextView
    private var endTextView: TextView
    private var endIconView: ImageView

    init {

        inflate(context, R.layout.widget_setting_item, this).apply {
            startIconView = findViewById(R.id.start_icon)
            titleView = findViewById(R.id.title)
            subTitleView = findViewById(R.id.sub_title)
            endTextView = findViewById(R.id.end_text)
            endIconView = findViewById(R.id.end_icon)
        }

        context.obtainStyledAttributes(attrs, R.styleable.SettingItem).apply {
            (0..indexCount).forEach { attr ->
                when (getIndex(attr)) {
                    R.styleable.SettingItem_siStartIcon -> {
                        startIconView.setImageResource(
                            getResourceId(R.styleable.SettingItem_siStartIcon, 0)
                        )
                    }
                    R.styleable.SettingItem_siTitle -> {
                        titleView.text = getString(R.styleable.SettingItem_siTitle)
                    }
                    R.styleable.SettingItem_siSubTitle -> {
                        subTitleView.text = getString(R.styleable.SettingItem_siSubTitle)
                        subTitleView.visibility = View.VISIBLE
                    }
                    R.styleable.SettingItem_siEndText -> {
                        endTextView.text = getString(R.styleable.SettingItem_siEndText)
                        endTextView.visibility = View.VISIBLE
                    }
                    R.styleable.SettingItem_siEndIcon -> {
                        endIconView.setImageResource(
                            getResourceId(R.styleable.SettingItem_siEndIcon, 0)
                        )
                    }
                }
            }
        }.also {
            it.recycle()
        }

        if (isInEditMode) {
            inflate(context, R.layout.widget_setting_item, this)
        }
    }

    var siSubTitle: String? = null
        set(value) {
            subTitleView.text = value
            subTitleView.visibility = if (value.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            field = value
        }
}