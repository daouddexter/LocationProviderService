package com.pds.locationproviderservice.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import com.pds.locationproviderservice.model.LocationHandler
import com.pds.locationproviderservice.model.request.RequestStubImpl

class LocationService : Service() {


    private val locationHandler: ILocationHandler by lazy {
        LocationHandler.getInstance()
    }

    private val requestStub: RequestStubImpl by lazy {
        RequestStubImpl(this, locationHandler)
    }

    //  Multiple clients can connect to the service at once.
    // However, the system calls your service's onBind() method to
    // retrieve the IBinder only when the first client binds.
    // The system then delivers the same IBinder to any additional clients that bind, without calling onBind() again.
    override fun onBind(intent: Intent): IBinder? {

        return requestStub
    }


    override fun onDestroy() {
        super.onDestroy()
        requestStub.reset()


    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }


}
