package com.ecreditpal.danaflash.ui.contact

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.ecreditpal.danaflash.model.ContactRes

class StartContact : ActivityResultContract<Void, Pair<String, ContactRes?>>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(context, ContactActivity::class.java)
    }

    override fun parseResult(resultCode: Int, result: Intent?): Pair<String, ContactRes?> {
        if (resultCode != Activity.RESULT_OK) {
            return Pair("-1", null)
        }
        return Pair(
            result?.getStringExtra(ContactActivity.EXTRA_CHOSEN_RESULT) ?: "-1",
            result?.getParcelableExtra(ContactActivity.EXTRA_CHOSEN_CONTACT)
        )
    }
}

