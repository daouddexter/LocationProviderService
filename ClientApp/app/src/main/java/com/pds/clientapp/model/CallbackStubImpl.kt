package com.pds.clientapp.model

import android.location.Location
import android.os.Process
import android.util.Log
import com.pds.locationproviderservice.ILocationCallbackInterface


class CallbackStubImpl : ILocationCallbackInterface.Stub() {


    override fun onCurrentLocation(location: Location?) {

    }

    override fun onPeriodicLocation(location: Location?) {
        Log.e(
            "Client App",
            "Current Location ${location?.latitude} ${location?.longitude} ${Thread.currentThread().name}"
        )
    }

    override fun getPid(): Int =
        Process.myPid()


}