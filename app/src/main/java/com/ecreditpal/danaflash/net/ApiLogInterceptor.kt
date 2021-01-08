package com.ecreditpal.danaflash.net

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class ApiLogInterceptor : Interceptor {

    private val headersToRedact = emptySet<String>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val sb = StringBuilder()
        val request = chain.request()
        val requestBody = request.body()
        val hasRequestBody = requestBody != null
        val connection = chain.connection()
        val requestStartMessage = ("--> "
                + request.method()
                + ' ' + request.url()
                + if (connection != null) " " + connection.protocol() else "")
        sb.append(requestStartMessage).append("\n")
        if (hasRequestBody) {
            // Request body headers are only present when installed as a network interceptor. Force
            // them to be included (when available) so there values are known.
            if (requestBody!!.contentType() != null) {
                sb.append(LEFT_BORDER).append("Content-Type: ").append(
                    requestBody.contentType()
                ).append("\n")
            }
            if (requestBody.contentLength() != -1L) {
                sb.append(LEFT_BORDER).append("Content-Length: ").append(
                    requestBody.contentLength()
                ).append("\n")
            }
        }
        val headers = request.headers()
        run {
            var i = 0
            val count = headers.size()
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(
                        name,
                        ignoreCase = true
                    ) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logHeader(headers, i, sb)
                }
                i++
            }
        }
        if (!hasRequestBody) {
            sb.append(LEFT_BORDER).append("--> END ").append(request.method()).append("\n")
        } else if (bodyHasUnknownEncoding(request.headers())) {
            sb.append(LEFT_BORDER).append("--> END ").append(request.method())
                .append(" (encoded body omitted)").append("\n")
        } else {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)
            var charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            sb.append(LEFT_BORDER).append("\n")
            if (isPlaintext(buffer)) {
                sb.append(LEFT_BORDER).append(buffer.readString(charset))
                sb.append(LEFT_BORDER).append("--> END ")
                    .append(request.method())
                    .append(" (")
                    .append(requestBody.contentLength())
                    .append("-byte body)")
                    .append("\n")
            } else {
                sb.append(LEFT_BORDER).append("--> END ")
                    .append(request.method())
                    .append(" (binary ")
                    .append(requestBody.contentLength())
                    .append("-byte body omitted)")
                    .append("\n")
            }
        }
        sb.append(LEFT_BORDER).append(MIDDLE_DIVIDER).append("\n")
        val startNs = System.nanoTime()
        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            sb.append(LEFT_BORDER).append("<-- HTTP FAILED: ").append(e).append("\n")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()
        sb.append(LEFT_BORDER).append("<-- ")
            .append(response.code())
            .append(if (response.message().isEmpty()) "" else ' '.toString() + response.message())
            .append(' ')
            .append(response.request().url())
            .append(" (")
            .append(tookMs)
            .append("ms")
            .append("")
            .append(')')
            .append("\n")
        var i = 0
        val count = headers.size()
        while (i < count) {
            logHeader(headers, i, sb)
            i++
        }
        if (!HttpHeaders.hasBody(response)) {
            sb.append(LEFT_BORDER).append("<-- END HTTP").append("\n")
        } else if (bodyHasUnknownEncoding(response.headers())) {
            sb.append(LEFT_BORDER).append("<-- END HTTP (encoded body omitted)").append("\n")
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer()
            var gzippedLength: Long? = null
            if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size()
                var gzippedResponseBody: GzipSource? = null
                try {
                    gzippedResponseBody = GzipSource(buffer.clone())
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                } finally {
                    gzippedResponseBody?.close()
                }
            }
            var charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            if (!isPlaintext(buffer)) {
                sb.append(LEFT_BORDER).append("\n")
                sb.append(LEFT_BORDER).append("<-- END HTTP (binary ").append(buffer.size())
                    .append("-byte body omitted)").append("\n")
                logIt(sb)
                return response
            }
            if (contentLength != 0L) {
                sb.append(LEFT_BORDER).append("\n")
                sb.append(LEFT_BORDER).append(buffer.clone().readString(charset)).append("\n")
            }
            if (gzippedLength != null) {
                sb.append(LEFT_BORDER).append("<-- END HTTP (")
                    .append(buffer.size())
                    .append("-byte, ")
                    .append(gzippedLength)
                    .append("-gzipped-byte body)")
            } else {
                sb.append(LEFT_BORDER).append("<-- END HTTP (").append(buffer.size())
                    .append("-byte body)")
            }
        }
        logIt(sb)
        return response
    }

    private fun logIt(sb: StringBuilder) {
        Log.d(
            TAG, """ 
$TOP_CORNER$SIDE_DIVIDER
$LEFT_BORDER$sb
$BOTTOM_CORNER$SIDE_DIVIDER"""
        )
    }

    private fun logHeader(headers: Headers, i: Int, sb: StringBuilder) {
        val value = if (headersToRedact.contains(headers.name(i))) "██" else headers.value(i)
        sb.append(LEFT_BORDER).append(headers.name(i)).append(": ").append(value)
    }

    companion object {
        private const val TOP_CORNER = "┌"
        private const val LEFT_BORDER = "│ "
        private const val BOTTOM_CORNER = "└"
        private const val SIDE_DIVIDER =
            "────────────────────────────────────────────────────────────────────────────────────────────────────────────────"
        private const val MIDDLE_DIVIDER =
            "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private val UTF8 = Charset.forName("UTF-8")
        private const val TAG = "OkHttpLog"

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        fun isPlaintext(buffer: Buffer): Boolean {
            return try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                true
            } catch (e: EOFException) {
                false // Truncated UTF-8 sequence.
            }
        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers["Content-Encoding"]
            return (contentEncoding != null && !contentEncoding.equals(
                "identity",
                ignoreCase = true
            )
                    && !contentEncoding.equals("gzip", ignoreCase = true))
        }
    }
}
