package com.ecreditpal.danaflash.helper

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ecreditpal.danaflash.R
import java.math.BigDecimal

@BindingAdapter("clickNav")
fun setClickNavigate(view: View?, @IdRes id: Int) {
    view.nav(id)
}

@BindingAdapter("android:visibleGone")
fun setVisibleOrGone(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("android:visibleInvisible")
fun setVisibleOrInvisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("circleImageUrl")
fun setCircleImage(imageView: ImageView, url: String?) {
    if (TextUtils.isEmpty(url)) {
        return
    }
    Glide.with(imageView).load(url).circleCrop().into(imageView)
}

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    if (TextUtils.isEmpty(url)) {
        return
    }
    Glide.with(imageView).load(url).into(imageView)
}

@BindingAdapter("avatarUrl")
fun setAvatarUrl(imageView: ImageView, url: String?) {
    if (TextUtils.isEmpty(url)) {
        imageView.setImageResource(R.drawable.ic_def_avatar)
        return
    }
    Glide.with(imageView).load(url).into(imageView)
}

@BindingAdapter("circleAvatarUrl")
fun setCircleAvatarUrl(imageView: ImageView, url: String?) {
    if (TextUtils.isEmpty(url)) {
        imageView.setImageResource(R.drawable.ic_def_avatar)
        return
    }
    Glide.with(imageView).load(url).circleCrop().into(imageView)
}

@BindingAdapter("rpAmount")
fun setRpAmount(textView: TextView, amount: BigDecimal?) {
    if (amount == null) {
        return
    }
    textView.text = String.format("Rp.%,d", amount.longValueExact())
}

/**
 * 设置印尼单位显示, 会根据数量单位来换成更高级单位
 */
@BindingAdapter("rupiahAmount")
fun setRupiahAmount(textView: TextView, amount: BigDecimal?) {
    if (amount == null) {
        return
    }
    val value = amount.longValueExact()
    var unit = ""
    var showAmount = 0L
    when {
        value < 1000 -> {
            unit = ""
            showAmount = value
        }
        value < 1_000_000 -> {
            unit = "Rb"
            showAmount = value / 1_000
        }
        value < 1_000_000_000 -> {
            unit = "Juta"
            showAmount = value / 1_000_000
        }
        value < 1_000_000_000_000 -> {
            unit = "Miliar"
            showAmount = value / 1_000_000_000
        }
    }
    textView.text = String.format("%d%s", showAmount, unit)
}
