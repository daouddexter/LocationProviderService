package com.pds.locationproviderservice.constants

import android.Manifest
import android.content.IntentFilter


object Constants {
    val permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    const val PERMISSION_REQUEST_CODE: Int = 12
    const val LOCATION_EXCEPTION_RESOLUTION = "LOCATION_EXCEPTION_RESOLUTION"
    const val REQUEST_CHECK_SETTINGS: Int = 9
    const val ACTION_LOCATION_PERMISSION_SUCCESS = "ACTION_LOCATION_PERMISSION_SUCCESS"
    const val ACTION_LOCATION_PERMISSION_FAILURE = "ACTION_LOCATION_PERMISSION_FAILURE"
    const val ACTION_LOCATION_SETTING_SUCCESS = "ACTION_LOCATION_SETTING_SUCCESS"
    const val ACTION_LOCATION_SETTING_FAILURE = "ACTION_LOCATION_SETTING_FAILURE"

    enum class LocationRegistrationResults {
       SUCCESS,FAILURE
    }


    val registrationBroadcastFilter = IntentFilter().apply {
        addAction(Constants.ACTION_LOCATION_SETTING_FAILURE)
        addAction(Constants.ACTION_LOCATION_PERMISSION_SUCCESS)
        addAction(Constants.ACTION_LOCATION_PERMISSION_FAILURE)
        addAction(Constants.ACTION_LOCATION_SETTING_SUCCESS)
    }

}

