package com.ecreditpal.danaflash.ui.personal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ecreditpal.danaflash.data.UserFace

class PersonalViewModel : ViewModel() {

    val phone = MutableLiveData<String>()

    fun updatePhone() {
        val number = UserFace.phone
        if (number.isBlank()) {
            phone.value = ""
            return
        }
        phone.value = number.replaceRange(
            number.length / 2 - 2,
            number.length / 2 + 2, "****"
        )
    }
}