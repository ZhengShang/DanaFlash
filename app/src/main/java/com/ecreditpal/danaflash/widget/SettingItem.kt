package com.ecreditpal.danaflash.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.ecreditpal.danaflash.R

@SuppressLint("Recycle")
class SettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var startIconView: ImageView
    private var titleView: TextView
    private var subTitleView: TextView
    private var endIconView: ImageView
    var endTextView: TextView

    init {

        inflate(context, R.layout.widget_setting_item, this).apply {
            startIconView = findViewById(R.id.start_icon)
            titleView = findViewById(R.id.title)
            subTitleView = findViewById(R.id.sub_title)
            endTextView = findViewById(R.id.end_text)
            endIconView = findViewById(R.id.end_icon)
        }

        if (!isInEditMode) {
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
                            siSubTitle = getString(R.styleable.SettingItem_siSubTitle)
                        }
                        R.styleable.SettingItem_siEndText -> {
                            siEndText = getString(R.styleable.SettingItem_siEndText)
                        }
                        R.styleable.SettingItem_siEndTextBackground -> {
                            endTextView.background =
                                getDrawable(R.styleable.SettingItem_siEndTextBackground)
                        }
                        R.styleable.SettingItem_siEndTextColor -> {
                            siEndTextColor = getColor(
                                R.styleable.SettingItem_siEndTextColor,
                                ContextCompat.getColor(context, R.color.text)
                            )
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

    var siEndText: String? = null
        set(value) {
            endTextView.text = value
            endTextView.visibility = if (value.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            field = value
        }

    var siEndTextColor: Int = ContextCompat.getColor(context, R.color.text)
        set(value) {
            endTextView.setTextColor(value)
            field = value
        }
}