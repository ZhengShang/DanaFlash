package com.ecreditpal.danaflash.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.ConvertUtils


class SideBarSortView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var mCanvas: Canvas? = null
    private var selectIndex = 0
    private var textSize = ConvertUtils.sp2px(12f)
    private var textColor = Color.GRAY
    private var textSizeChoose = ConvertUtils.sp2px(14f)
    private var textColorChoose = Color.RED

    var list = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"
    )
    private var paint = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mCanvas = canvas
        paintText()
    }

    private fun paintText() {
        //计算每一个字母的高度,总告诉除以字母集合的高度就可以
        val height = height / list.size
        for (i in list.indices) {
            if (i == selectIndex) {
                paint.color = textColorChoose
                paint.textSize = textSizeChoose.toFloat()
            } else {
                paint.color = textColor
                paint.textSize = textSize.toFloat()
            }
            paint.isAntiAlias = true //设置抗锯齿
            paint.typeface = Typeface.DEFAULT_BOLD
            //计算每一个字母x轴
            val paintX = width / 2f - paint.measureText(list[i]) / 2
            //计算每一个字母Y轴
            val paintY = height * i + height
            //绘画出来这个TextView
            mCanvas!!.drawText(list[i], paintX, paintY.toFloat(), paint)
            //画完一个以后重置画笔
            paint.reset()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val index = (event.y / height * list.size).toInt()
                if (index >= 0 && index < list.size) {
                    mClickListener?.onSideBarScrollUpdateItem(list[index])
                    selectIndex = index
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (mClickListener != null) {
                mClickListener?.onSideBarScrollEndHideText()
            }
        }
        return true
    }

    private var mClickListener: OnIndexChangedListener? = null

    interface OnIndexChangedListener {
        fun onSideBarScrollUpdateItem(word: String?) //滚动位置
        fun onSideBarScrollEndHideText() //隐藏提示文本
    }

    fun setIndexChangedListener(listener: OnIndexChangedListener?) {
        mClickListener = listener
    }

    fun onitemScrollUpdateText(word: String) {
        for (i in list.indices) {
            if (list[i] == word && selectIndex != i) {
                selectIndex = i
                invalidate()
            }
        }
    }
}