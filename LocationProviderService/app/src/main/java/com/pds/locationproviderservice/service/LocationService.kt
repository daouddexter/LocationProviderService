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

    override fun onBind(intent: Intent): IBinder? {

        return requestStub
    }


    override fun onDestroy() {
        super.onDestroy()
        requestStub.reset()


    }


}
