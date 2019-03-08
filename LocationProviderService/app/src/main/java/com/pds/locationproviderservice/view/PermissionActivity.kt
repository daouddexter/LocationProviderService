package com.pds.locationproviderservice.view

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.pds.locationproviderservice.R
import com.pds.locationproviderservice.constants.Constants
import com.pds.locationproviderservice.constants.Constants.PERMISSION_REQUEST_CODE
import com.pds.locationproviderservice.constants.Constants.permissions
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import com.pds.locationproviderservice.model.LocationHandler
import com.pds.locationproviderservice.service.LocationService


class PermissionActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)




        if (intent?.action == Intent.ACTION_MAIN) {
            startService(Intent(this, LocationService::class.java))
          //  finish()
            return
        }
        val isForLocationSetting: Boolean =
            intent?.getBooleanExtra(Constants.LOCATION_EXCEPTION_RESOLUTION, false) ?: false
        if (isForLocationSetting) {

            try {
                val locationHandler: ILocationHandler = LocationHandler.getInstance()

                (locationHandler as LocationHandler).failure?.startResolutionForResult(
                    this@PermissionActivity,
                    Constants.REQUEST_CHECK_SETTINGS
                )
            } catch (e: IntentSender.SendIntentException) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
                    action = Constants.ACTION_LOCATION_SETTING_FAILURE
                })
            }

        } else {

            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)

        }


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionGranted = true
        if (requestCode == Constants.PERMISSION_REQUEST_CODE) {
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED)
                    permissionGranted = false
            }
        }
        val action =
            if (permissionGranted)
                Constants.ACTION_LOCATION_PERMISSION_SUCCESS
            else
                Constants.ACTION_LOCATION_PERMISSION_FAILURE

        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
            this.action = action
        })
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
            val result =
                if (resultCode == Activity.RESULT_OK)
                    Constants.ACTION_LOCATION_SETTING_SUCCESS
                else
                    Constants.ACTION_LOCATION_SETTING_FAILURE

            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent().apply {
                action = result
            })
        }
    }
}
