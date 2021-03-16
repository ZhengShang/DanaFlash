package com.ecreditpal.danaflash.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ecreditpal.danaflash.net.dfApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.RequestBody

class UploadSurveyWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    companion object {
        const val EXTRA_SURVEY_STRING = "EXTRA_SURVEY_STRING"
    }

    override fun doWork(): Result {
        val surveyString = inputData.getString(EXTRA_SURVEY_STRING)

        val json = """
            {
                "__topic__": "survey",
                "__logs__": {"survey":$surveyString}
            }
        """.trimIndent()

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), json
        )

        val success = runBlocking {
            try {
                dfApi().uploadSurvey(
                    "http://tropic.cn-hongkong.log.aliyuncs.com/logstores/staging/track",
                    body.contentLength(),
                    body
                )
                true
            } catch (e: Exception) {
                println("upload survey failed. $e")
                false
            }
        }

        return if (success) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
