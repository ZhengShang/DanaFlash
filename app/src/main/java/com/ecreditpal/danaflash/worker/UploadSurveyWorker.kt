package com.ecreditpal.danaflash.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ecreditpal.danaflash.helper.SurveyHelper
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.RequestBody

class UploadSurveyWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        val uploadList = SurveyHelper.surveyList
        if (uploadList.isEmpty()) {
            return Result.failure()
        }

        val listJson = uploadList.joinToString { it.toString() }

        val json = """
            {
                "__topic__": "survey",
                "__logs__": [$listJson]
            }
        """.trimIndent()

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), json
        )

        val res = runBlocking {
            kotlin.runCatching {
                dfApi().uploadSurvey(body.contentLength(), body).throwIfNotSuccess()
            }.getOrNull()
        } ?: return Result.retry()

        return if (res.isSuccess()) {
            SurveyHelper.surveyList.removeAll(uploadList)
            Result.success()
        } else {
            Result.retry()
        }
    }
}
