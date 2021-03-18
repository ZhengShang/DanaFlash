package com.ecreditpal.danaflash.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactRes(
    var name: String?,
    var phone: String?
) : Parcelable