package com.ecreditpal.danaflash.helper

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ecreditpal.danaflash.R

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
