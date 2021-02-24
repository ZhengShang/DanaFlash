package com.ecreditpal.danaflash.js

class WebInterface {

    companion object {
        private const val JS_PREFIX = "javascript:"
    }

    /**
     * 通知前端图⽚上传状态
     * "1" 上传成功
     * "0" 开始上传
     * "-1" 上传失败
     */
    fun isUploading(status: String): String {
        return JS_PREFIX + "isUploading($status)"
    }

    /**
     * 将图⽚链接发送给前端
    传参（jsonString）
    url 图⽚链接
    type 图⽚类型（前端在调⽤launchOCR后传递的值
    img 图⽚的base64编码(NO_WRAP)
     */
    fun sendImgUrl(url: String, type: String?, img: String): String {
        val json = """
            "{\"url\":\"$url\", \"type\":\"$type\", \"img\":\"data:image/jpeg;base64,$img\"}"
        """.trimIndent()
        return JS_PREFIX + "sendImgUrl($json)"
    }

    /**
     * 通知前端，⽤户从OCR相机界⾯返回
     */
    fun ocrBack(): String {
        return JS_PREFIX + "ocrBack()"
    }

    /**
     *通知前端，⽤户从后台返回前台
     */
    fun ReturnForeground(): String {
        return JS_PREFIX + "ReturnForeground()"
    }

    /**
     * 通知前端，⽤户按了⼿机物理返回
     * 发⽣于onBackPressed事件
     */
    fun backPress(): String {
        return JS_PREFIX + "backPress()"
    }

    /**
     * 参考 getReferer
     *   在收到回调后将response.installReferrer部分值，通过此接⼝通知前端
     */
    fun sendRefer(json: String): String {
        return JS_PREFIX + "sendRefer($json)"
    }

    fun sendMediaSource(): String {
        return JS_PREFIX + "sendMediaSource()"
    }

    /**
     * 回调前端的接口
     * @param methodName 对应的方法名
     * @param jsonResult 结果
     */
    fun sendCallback(methodName: String, jsonResult: String): String {
        return JS_PREFIX + "webview_back['$methodName']('$jsonResult')"
    }
}