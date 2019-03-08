package com.pds.locationproviderservice.model

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.pds.locationproviderservice.constants.Constants
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import com.pds.locationproviderservice.view.PermissionActivity
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.BehaviorSubject

class LocationHandler private constructor() : ILocationHandler, LocationCallback() {

    companion object {
        private val locationHandler: LocationHandler by lazy {
            LocationHandler()
        }


        fun getInstance() = locationHandler
    }

    var failure: ResolvableApiException? = null
        private set

    private var mRegistered: Boolean = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationSubject: BehaviorSubject<Location> = BehaviorSubject.create()

    override fun getLocationSubject(): BehaviorSubject<Location> = locationSubject

    override fun registerForLocationUpdate(
        context: Context,
        interval: Long
    ): Single<Constants.LocationRegistrationResults> =
        Single.defer<Constants.LocationRegistrationResults> {
            Single.create<Constants.LocationRegistrationResults> { emitter ->
                mRegistered = false
                if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
                    val locationRequest: LocationRequest = LocationRequest.create().apply {
                        this.interval = interval
                        fastestInterval = (interval / 2)
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    }
                    val receiver = object : BroadcastReceiver() {
                        @SuppressLint("MissingPermission")
                        override fun onReceive(c: Context?, intent: Intent?) {

                            Log.e("Location handler", "Received in handler")

                            when (intent?.action) {
                                Constants.ACTION_LOCATION_PERMISSION_SUCCESS -> onPermissionGranted(
                                    context,
                                    locationRequest,
                                    emitter
                                )
                                Constants.ACTION_LOCATION_PERMISSION_FAILURE -> emitter.onSuccess(Constants.LocationRegistrationResults.FAILURE)
                                Constants.ACTION_LOCATION_SETTING_SUCCESS -> {
                                    fusedLocationProviderClient.requestLocationUpdates(
                                        locationRequest,
                                        this@LocationHandler,
                                        null
                                    )
                                    emitter.onSuccess(Constants.LocationRegistrationResults.SUCCESS)
                                }
                                Constants.ACTION_LOCATION_SETTING_FAILURE -> emitter.onSuccess(Constants.LocationRegistrationResults.FAILURE)

                            }


                        }

                    }
                    emitter.setDisposable(Disposables.fromRunnable {
                        if (mRegistered)
                            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
                    })
                    LocalBroadcastManager.getInstance(context)
                        .registerReceiver(receiver, Constants.registrationBroadcastFilter)
                    mRegistered = true
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        onPermissionGranted(context, locationRequest, emitter)
                    } else {
                        context.startActivity(Intent(context, PermissionActivity::class.java))
                    }

                } else {
                    emitter.onSuccess(Constants.LocationRegistrationResults.FAILURE)

                }

            }


        }


    override fun unregisterFromLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(this@LocationHandler)
    }


    override fun onLocationResult(locationResult: LocationResult?) {

        locationResult?.locations?.forEach {
            locationSubject.onNext(it)
        }
    }


    @SuppressLint("MissingPermission")
    private fun onPermissionGranted(
        context: Context,
        locationRequest: LocationRequest,
        emitter: SingleEmitter<Constants.LocationRegistrationResults>
    ) {
        val lastLocation = fusedLocationProviderClient.lastLocation
        lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                locationSubject.onNext(it)
            }
        }


        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, this@LocationHandler, null)
            emitter.onSuccess(Constants.LocationRegistrationResults.SUCCESS)

        }
        task.addOnFailureListener { failure ->
            if (failure is ResolvableApiException) {
                this@LocationHandler.failure = failure
                context.startActivity(Intent(context, PermissionActivity::class.java).apply {
                    putExtra(Constants.LOCATION_EXCEPTION_RESOLUTION, true)

                })
            } else {
                emitter.onSuccess(Constants.LocationRegistrationResults.FAILURE)

            }
        }
    }


}