package com.ecreditpal.danaflash.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.ConvertUtils
import kotlin.math.min


class SideBarSortView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var selectIndex = 0
    private val TEXT_SIZE = ConvertUtils.sp2px(12f)
    private val TEXT_COLOR = Color.GRAY
    private val TEXT_SIZE_CHOOSE = ConvertUtils.sp2px(14f)
    private val TEXT_COLOR_CHOOSE = Color.RED

    var list = mutableListOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#"
    )
    private var paint = Paint().apply {
        isAntiAlias = true //设置抗锯齿
        typeface = Typeface.DEFAULT_BOLD
        textSize = TEXT_SIZE_CHOOSE.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = (measuredHeight - paddingTop - paddingBottom) / list.size
        for (i in list.indices) {
            if (i == selectIndex) {
                paint.color = TEXT_COLOR_CHOOSE
                paint.textSize = TEXT_SIZE_CHOOSE.toFloat()
            } else {
                paint.color = TEXT_COLOR
                paint.textSize = TEXT_SIZE.toFloat()
            }
            //计算每一个字母x轴
            val paintX = width / 2f - paint.measureText(list[i]) / 2
            //计算每一个字母Y轴
            val textYHeight = paint.fontMetrics.let { it.descent - it.ascent }
            val paintY = height * i + (height + textYHeight) / 2 + paddingTop
            //绘画出来这个TextView
            canvas.drawText(list[i], paintX, paintY, paint)
            //画完一个以后重置画笔
            paint.reset()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val charHeight = paint.fontMetrics.let {
            it.descent - it.ascent
        }.toInt() + 10 //微微增大一点间距
        val desiredHeight = list.size * charHeight + paddingTop + paddingBottom

        setMeasuredDimension(
            measureDimension(desiredWidth, widthMeasureSpec),
            measureDimension(desiredHeight, heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        if (result < desiredSize) {
            Log.e("SideBarSortView", "The view is too small, the content might get cut")
        }
        return result
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val index = (event.y / height * list.size).toInt()
                if (index >= 0 && index != selectIndex && index < list.size) {
                    clickListener?.onSideBarScrollUpdateItem(list[index])
                    selectIndex = index
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                clickListener?.onSideBarScrollEndHideText()
            }
        }
        return true
    }

    var clickListener: OnIndexChangedListener? = null

    interface OnIndexChangedListener {
        fun onSideBarScrollUpdateItem(word: String?) //滚动位置
        fun onSideBarScrollEndHideText() //隐藏提示文本
    }

    fun onItemScrollUpdateText(word: String) {
        val index = list.indexOf(word)
        if (index > 0) {
            if (selectIndex != index) {
                selectIndex = index
                invalidate()
            }
        }
    }

    fun setValidLabels(labels: List<Any>?) {
        if (labels.isNullOrEmpty()) {
            return
        }
        list.retainAll {
            it in labels
        }
        if (list.isEmpty()) {
            return
        }
        selectIndex = 0
        invalidate()
    }
}