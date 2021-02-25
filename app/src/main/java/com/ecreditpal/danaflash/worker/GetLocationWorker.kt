package com.ecreditpal.danaflash.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.ecreditpal.danaflash.data.UserFace
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.CountDownLatch

class GetLocationWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return Result.failure()
        }

        if (UserFace.location != null) {
            //已经有定位数据了, 就不重复定位了
            return Result.success()
        }

        val latch = CountDownLatch(1)
        try {

            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation
                .addOnSuccessListener { lastLocation: Location? ->
                    if (lastLocation == null) {
                        client.requestLocationUpdates(
                            LocationRequest.create(),
                            object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult?) {
                                    locationResult ?: return
                                    locationResult.locations.firstOrNull()?.let {
                                        UserFace.location = it
                                        LogUtils.d("Get location result: lon = ${it.longitude}, lat = ${it.latitude}")
                                        client.removeLocationUpdates(this)
                                        latch.countDown()
                                    }
                                }
                            },
                            Looper.getMainLooper()
                        )
                    } else {
                        UserFace.location = lastLocation
                        LogUtils.d("Get last location: lon = ${lastLocation.longitude}, lat = ${lastLocation.latitude}")
                        latch.countDown()
                    }
                }
                .addOnFailureListener {
                    LogUtils.e("Get location failed.", it)
                    latch.countDown()
                }

            latch.await()
        } catch (e: Exception) {
            e.printStackTrace()
            latch.countDown()
        }

        return if (UserFace.location == null) {
            Result.failure()
        } else {
            Result.success()
        }
    }
}