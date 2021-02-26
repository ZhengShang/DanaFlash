package com.ecreditpal.danaflash.model

import com.alibaba.fastjson.annotation.JSONField

class DeviceInfoBean {
    var hardware: Hardware? = null
    var storage: Storage? = null
    var general_data: General? = null
    var other_data: Other? = null
    var application: List<AppInfo?>? = null
    var network: NetworkBean? = null
    var location: LocationBean? = null
    var battery_status: BatteryStatus? = null
    var contact: List<Contact?>? = null
    var audio_internal: String? = null
    var audio_external: String? = null
    var images_internal: String? = null
    var images_external: String? = null
    var video_internal: String? = null
    var video_external: String? = null
    var download_files: String? = null
    var contact_group: String? = null


    class Hardware {
        var device_name: String? = null
        var sdk_version: String? = null
        var model: String? = null
        var physical_size: String? = null
        var release: String? = null
        var brand: String? = null
        var serial_number: String? = null
    }

    class Storage {
        var ram_total_size: String? = null
        var ram_usable_size: String? = null
        var memory_card_size: String? = null
        var memory_card_size_use: String? = null
        var internal_storage_total: String? = null
        var internal_storage_usable: String? = null
    }

    class General {
        var gaid: String? = null
        var and_id: String? = null
        var phone_type: String? = null
        var mac: String? = null
        var language: String? = null
        var locale_display_language: String? = null
        var locale_iso3_language: String? = null
        var locale_iso3_country: String? = null
        var imei: String? = null
        var phone_number: String? = null
        var network_operator_name: String? = null
        var network_type: String? = null
        var time_zone_id: String? = null
    }

    class Other {
        var root_jailbreak: String? = null
        var last_boot_time: String? = null
        var keyboard: String? = null
        var simulator: String? = null
        var dbm: String? = null
    }

    class AppInfo {
        var app_name: String? = null
        var `package`: String? = null
        var in_time: String? = null
        var app_type: Int = 0
        var version_name: String? = null
        var version_code: String? = null
        var flags: Int = 0
        var up_time: String? = null
    }

    class NetworkBean {
        var IP: String? = null
        var current_wifi: CurrentWifi? = null
        var configured_wifi: List<CurrentWifi>? = null
    }

    class CurrentWifi {
        var ssid: String = ""
        var name: String = ""
        var bssid: String = ""
        var mac: String = ""

    }

    class LocationBean {
        var gps: GPS? = null
    }

    class GPS {
        var latitude: String = ""
        var longitude: String = ""

    }

    class BatteryStatus {
        var battery_pct: String? = null

        @JSONField(name = "is_charging")
        var is_charging: String? = null

        @JSONField(name = "is_ac_charge")
        var is_ac_charge: String? = null

        @JSONField(name = "is_usb_charge")
        var is_usb_charge: String? = null
    }

    class Contact {
        var contact_display_name: String? = null
        var last_time_contacted: String? = null
        var number: String? = null
        var times_contacted: String? = null
        var up_time: String? = null
    }
}