package com.pds.locationproviderservice.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import com.pds.locationproviderservice.constants.Constants
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import com.pds.locationproviderservice.model.LocationData
import com.pds.locationproviderservice.model.LocationHandler
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable

class LocationService : Service() {

    private var mRegistered = false
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val locationHandler: ILocationHandler by lazy {
        LocationHandler.getInstance()
    }

    override fun onCreate() {
        super.onCreate()
        disposable.add(locationHandler.registerForLocationUpdate(this, 2000).subscribe({
            mRegistered = it == Constants.LocationRegistrationResults.SUCCESS
        }, {
            mRegistered = false
        }))


    }

    override fun onBind(intent: Intent): IBinder? {

        return null
    }


    private fun registerLocationListener() {
        disposable.add(locationHandler.getLocationSubject().toFlowable(BackpressureStrategy.LATEST).subscribe({
            Log.e("Location", "Location is ${it.latitude} ${it.longitude}")
        }, {

        }))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposable.isDisposed)
            disposable.dispose()


    }




}
