package com.ecreditpal.danaflash.base

/**
 * 权限申明弹窗弹出顺序优先于引导弹窗（弹窗顺序：权限申明弹窗>引导更新弹窗>API弹窗>pop弹窗）
 */
object PopManager {
    const val TYPE_UPDATE = 0
    const val TYPE_API = 1
    const val TYPE_POP = 2

    /**
     * 存放首页的3中弹窗的Map, 分别为更新弹窗, API和POP弹窗.
     * 用这个管理他们弹出的顺序
     */
    private val mutableMap = mutableMapOf<Int, Any?>()
    private var showing = false

    fun addPopToMap(type: Int, value: Any?) {
        if (showing.not()) {
            mutableMap[type] = value
        }
    }

    fun next(): Pair<Int, Any>? {
        return when {
            mutableMap[TYPE_UPDATE] != null -> {
                Pair(TYPE_UPDATE, mutableMap.remove(TYPE_UPDATE)!!)
            }
            mutableMap[TYPE_API] != null -> {
                Pair(TYPE_API, mutableMap.remove(TYPE_API)!!)
            }
            mutableMap[TYPE_POP] != null -> {
                Pair(TYPE_POP, mutableMap.remove(TYPE_POP)!!)
            }
            else -> null
        }
    }

    fun getNextShowPopValue(): Pair<Int, Any>? {
        if (showing) {
            val pair = next()
            if (pair == null) {
                showing = false
            }
            return pair
        }
        if (mutableMap.size != 3) {
            return null
        }
        showing = true
        return getNextShowPopValue()
    }
}