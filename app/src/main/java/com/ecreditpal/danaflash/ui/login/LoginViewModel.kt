package com.ecreditpal.danaflash.ui.login

import DataStoreKeys
import android.app.Application
import android.os.CountDownTimer
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.helper.writeDsData
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val countDownRemain = MutableLiveData(60)
    val loginResult = MutableLiveData<Boolean>()

    private val timer: CountDownTimer by lazy {
        object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownRemain.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                countDownRemain.value = 0
            }
        }
    }

    val coldDownText = Transformations.map(countDownRemain) { "${it}s" }
    val verifyCodeBtnVisible = Transformations.map(countDownRemain) { it == 0 || it == 60 }
    val verifyCodeString = Transformations.map(countDownRemain) {
        when (it) {
            0 -> application.getString(R.string.reacquire)
            else -> application.getString(R.string.get_verification_code)
        }
    }


    fun getVerifyCode(phone: Editable) {
        if (validPhone(phone.toString()).not()) {
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            LoadingTips.showLoading()
            val res = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    dfApi().getVCode(phone.toString()).throwIfNotSuccess()
                }.getOrNull()
            }
            LoadingTips.dismissLoading()

            if (res?.isSuccess() == true) {
                startCountDown()
            } else {
                ToastUtils.showLong(R.string.failed_to_get_verify_code)
            }
        }
    }

    fun login(phone: Editable, code: Editable) {
        if (validPhone(phone.toString()).not()) {
            return
        }
        viewModelScope.launch {
            LoadingTips.showLoading()
            val res = kotlin.runCatching {
                dfApi().login(
                    phone.toString(),
                    UserFace.getGoogleId(),
                    code.toString()
                ).throwIfNotSuccess()
            }.getOrNull()
            LoadingTips.dismissLoading()

            if (res?.isSuccess() == true) {
                viewModelScope.launch(Dispatchers.IO) {
                    UserFace.token = res.data?.token ?: ""
                    UserFace.phone = res.data?.number ?: ""
                    App.context.writeDsData(DataStoreKeys.TOKEN, res.data?.token ?: "")
                    App.context.writeDsData(DataStoreKeys.PHONE, res.data?.nationalNumber ?: "")
                }
                loginResult.value = true
            } else {
                ToastUtils.showLong(R.string.failed_to_login)
            }
        }
    }

    private fun validPhone(phone: String): Boolean {

        //Find first none 0 char
        var indexOfNoneZero = 0
        for (i in 0..phone.length) {
            if ('0' == phone[i]) {
                indexOfNoneZero = i
                continue
            } else {
                break
            }
        }

        //compare if first none 0 is digit 8
        if (phone[indexOfNoneZero] != '8') {
            ToastUtils.showLong(R.string.dismatch_phone_with_first_not_8)
            return false
        }

        if (phone.length < 9) {
            ToastUtils.showLong(R.string.dismatch_phone_with_first_not_8)
            return false
        }

        if (RegexUtils.isMatch("^8[0-9]{8,12}$", phone).not()) {
            ToastUtils.showLong(R.string.dismatch_phone_with_first_not_8)
            return false
        }
        return true
    }

    private fun startCountDown() {
        timer.cancel()
        timer.start()
    }

    override fun onCleared() {
        timer.cancel()
        super.onCleared()
    }
}