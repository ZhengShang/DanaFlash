package com.ecreditpal.danaflash.ui.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ecreditpal.danaflash.data.UserFace

class PersonalViewModel : ViewModel() {

    val phone = liveData<String> { UserFace.phone }
}