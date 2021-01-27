package com.ecreditpal.danaflash.ui.camera

import ai.advance.liveness.lib.*
import ai.advance.liveness.lib.Detector.*
import ai.advance.liveness.lib.http.entity.ResultEntity
import ai.advance.liveness.lib.impl.LivenessCallback
import ai.advance.liveness.lib.impl.LivenessGetFaceDataCallback
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableInt
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import com.ecreditpal.danaflash.base.LoadingTips
import com.ecreditpal.danaflash.databinding.FragmentLivenessBinding
import com.ecreditpal.danaflash.ui.comm.ConfirmDialog
import org.json.JSONObject

class LivenessFragment : BaseFragment() {

    private lateinit var binding: FragmentLivenessBinding
    private val tipsResIdOb = ObservableInt()
    private val timerSecOb = ObservableInt()

    /**
     * the array of tip imageView animationDrawable
     * 动作提示 imageView 的图像集合
     */
    private val drawableCache: SparseArray<AnimationDrawable> = SparseArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        val jsonStr = activity?.intent?.getStringExtra(CameraActivity.KEY_JSON)
        if (jsonStr.isNullOrEmpty()) {
            activity?.finish()
            return
        }

        var key = ""
        var secret = ""
        kotlin.runCatching {
            JSONObject(jsonStr).let {
                key = it.optString("key")
                secret = it.optString("secret")
            }
        }.onFailure {
            LogUtils.e("parse liveness json key and secret failed.")
            activity?.finish()
            return
        }

        GuardianLivenessDetectionSDK.init(
            activity?.application,
            key,
            secret,
            Market.Indonesia
        )

        changeAppBrightness(255)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLivenessBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tipsRes = tipsResIdOb
        binding.timerSec = timerSecOb
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.finish() }
        view.findViewById<TextView>(R.id.title).text = getString(R.string.liveness)
    }

    override fun onResume() {
        super.onResume()
        if (GuardianLivenessDetectionSDK.isDeviceSupportLiveness()) {
            tryStart()
        } else {
            showTipsDialogAndFinish(getString(R.string.liveness_device_not_support))
        }
    }

    override fun onStop() {
        super.onStop()
        binding.livenessView.stopDetection()
    }

    private fun tryStart() {
        val ctx = context ?: return
        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.livenessView.startDetection(livenessCallback)
        }
    }

    override fun onDestroy() {
        binding.livenessView.destory()
        changeAppBrightness(-1)
        super.onDestroy()
    }

    /**
     * update tip textView text
     * 更新提示文本的文案
     *
     * @param warnCode the status of current frame 当前的状态
     */
    private fun updateTipUIView(warnCode: WarnCode?) {
        if (binding.livenessView.isVertical) { //phone not vertical
            if (warnCode != null) {
                when (warnCode) {
                    WarnCode.FACEMISSING -> tipsResIdOb.set(R.string.liveness_no_people_face)
                    WarnCode.FACESMALL -> tipsResIdOb.set(R.string.liveness_tip_move_closer)
                    WarnCode.FACELARGE -> tipsResIdOb.set(R.string.liveness_tip_move_furthre)
                    WarnCode.FACENOTCENTER -> tipsResIdOb.set(R.string.liveness_move_face_center)
                    WarnCode.FACENOTFRONTAL -> tipsResIdOb.set(R.string.liveness_frontal)
                    WarnCode.FACENOTSTILL, WarnCode.FACECAPTURE -> tipsResIdOb.set(R.string.liveness_still)
                    WarnCode.FACEINACTION -> showActionTipUIView()
                    else -> {
                    }
                }
            } else {
                tipsResIdOb.set(0)
            }
        } else {
            tipsResIdOb.set(R.string.liveness_hold_phone_vertical)
        }
    }

    /**
     * show current action tips
     * 显示当前动作的动画提示
     */
    private fun showActionTipUIView() {
        val currentDetectionType: DetectionType = binding.livenessView.currentDetectionType
        var detectionNameId = 0
        when (currentDetectionType) {
            DetectionType.POS_YAW -> detectionNameId = R.string.liveness_pos_raw
            DetectionType.MOUTH -> detectionNameId = R.string.liveness_mouse
            DetectionType.BLINK -> detectionNameId = R.string.liveness_blink
            else -> {
            }
        }
        tipsResIdOb.set(detectionNameId)
        val anim: AnimationDrawable = getDrawRes(currentDetectionType)
        binding.tipImageView.setImageDrawable(anim)
        anim.start()
    }

    /**
     * Get the prompt picture/animation according to the action type
     * 根据动作类型获取动画资源
     *
     * @param detectionType Action type 动作类型
     * @return Prompt picture/animation
     */
    private fun getDrawRes(detectionType: DetectionType?): AnimationDrawable {
        var resID = -1
        if (detectionType != null) {
            when (detectionType) {
                DetectionType.POS_YAW -> resID = R.drawable.anim_frame_turn_head
                DetectionType.MOUTH -> resID = R.drawable.anim_frame_open_mouse
                DetectionType.BLINK -> resID = R.drawable.anim_frame_blink
            }
        }
        val cachedDrawAble: AnimationDrawable? = drawableCache.get(resID)
        return if (cachedDrawAble == null) {
            val drawable =
                ContextCompat.getDrawable(binding.root.context, resID) as AnimationDrawable
            drawableCache.put(resID, drawable)
            drawable
        } else {
            cachedDrawAble
        }
    }

    private fun setResultData(data: String) {
        activity?.setResult(
            Activity.RESULT_OK,
            Intent().putExtra(LivenessFragment.EXTRA_RESULT, data)
        )
        activity?.finish()
    }

    private val livenessCallback = object : LivenessCallback {

        /**
         * called by when detection auth start
         * 活体检测授权开始时会执行该方法
         */
        override fun onDetectorInitStart() {
            LoadingTips.showLoading()
        }

        /**
         * called by when detection auth complete
         * 活体检测授权完成后会执行该方法
         *
         * @param isValid   whether the auth is success 活体检测是否成功
         * @param errorCode the error code 错误码
         * @param message   the error message 错误信息
         */
        override fun onDetectorInitComplete(
            isValid: Boolean,
            errorCode: String?,
            message: String?
        ) {
            LoadingTips.dismissLoading()
            if (isValid) {
                updateTipUIView(null)
            } else {
                val errorMessage = if (errorCode == LivenessView.NO_RESPONSE) {
                    getString(R.string.liveness_failed_reason_auth_failed)
                } else {
                    message
                }
                showTipsDialogAndFinish(errorMessage)
            }
        }

        /**
         * called by first action start or after an action finish
         * 当准备阶段完成时，以及每个动作完成后，会执行该方法
         */
        override fun onDetectionSuccess() {
            binding.livenessView.getLivenessData(object : LivenessGetFaceDataCallback {
                override fun onGetFaceDataStart() {
                    LoadingTips.showLoading()
                }

                override fun onGetFaceDataSuccess(entity: ResultEntity, livenessId: String) {
                    LoadingTips.dismissLoading()
                    // liveness detection success
                    setResultData(livenessId)
                }

                override fun onGetFaceDataFailed(entity: ResultEntity) {
                    LoadingTips.dismissLoading()
                    if (!entity.success && LivenessView.NO_RESPONSE == entity.code) {
                        LivenessResult.setErrorMsg(getString(R.string.liveness_failed_reason_bad_network))
                    }
                    setResultData(LIVENESS_FAILED)
                }
            })
        }

        /**
         * called by current frame is warn or become normal,is necessary to update tip UI
         * 当前帧的状态发生异常或者从异常状态变为正常的时候，需要更新 UI 上的提示语
         *
         * @param warnCode status of current frame 本帧的状态
         */
        override fun onDetectionFrameStateChanged(warnCode: Detector.WarnCode?) {
            if (isAdded) {
                updateTipUIView(warnCode)
            }
        }

        /**
         * called by Remaining time changed of current action,is necessary to update countdown timer view
         * 当前动作剩余时间变化,需要更新倒计时控件上的时间
         *
         * @param remainingTimeMills remaining time of current action 毫秒单位的剩余时间
         */
        override fun onActionRemainingTimeChanged(remainingTimeMills: Long) {
            if (isAdded) {
                val mills = (remainingTimeMills / 1000).toInt()
                timerSecOb.set(mills)
            }
        }

        /**
         * called by detection failed
         * 活体检测失败时的回调
         *
         * @param failedType    Type of failures 失败的类型
         * @param detectionType Type of action 失败的原因
         */
        override fun onDetectionFailed(
            failedType: DetectionFailedType?,
            detectionType: DetectionType?
        ) {
            if (isAdded) {
                when (failedType) {
                    DetectionFailedType.WEAKLIGHT -> tipsResIdOb.set(R.string.liveness_weak_light)
                    DetectionFailedType.STRONGLIGHT -> tipsResIdOb.set(R.string.liveness_too_light)
                    else -> {
                        var errorMsg: String? = null
                        when (failedType) {
                            DetectionFailedType.FACEMISSING -> when (detectionType) {
                                DetectionType.MOUTH, DetectionType.BLINK -> errorMsg =
                                    getString(R.string.liveness_failed_reason_facemissing_blink_mouth)
                                DetectionType.POS_YAW -> errorMsg =
                                    getString(R.string.liveness_failed_reason_facemissing_pos_yaw)
                            }
                            DetectionFailedType.TIMEOUT -> errorMsg =
                                getString(R.string.liveness_failed_reason_timeout)
                            DetectionFailedType.MULTIPLEFACE -> errorMsg =
                                getString(R.string.liveness_failed_reason_multipleface)
                            DetectionFailedType.MUCHMOTION -> errorMsg =
                                getString(R.string.liveness_failed_reason_muchaction)
                        }
                        LivenessResult.setErrorMsg(errorMsg)
                        setResultData(LIVENESS_FAILED)
                    }
                }
            }
        }


        /**
         * called by first action start or after an action finish
         * 当准备阶段完成时，以及每个动作完成后，会执行该方法
         */
        override fun onDetectionActionChanged() {
            showActionTipUIView()
        }

    }

    private fun showTipsDialogAndFinish(content: String?) {
        ConfirmDialog(
            titleStr = getString(R.string.prompt),
            contentStr = content,
            negativeStr = "",
            positiveClickListener = {
                activity?.finish()
            }
        ).apply {
            isCancelable = false
        }.show(childFragmentManager)
    }

    /**
     * set current activity brightness to max
     * 将当前页面的亮度调节至最大
     */
    private fun changeAppBrightness(brightness: Int) {
        val window: Window = activity?.window ?: return
        val lp = window.attributes
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        } else {
            lp.screenBrightness = (if (brightness <= 0) 1 else brightness) / 255f
        }
        window.attributes = lp
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        private const val LIVENESS_FAILED = "0"
    }
}