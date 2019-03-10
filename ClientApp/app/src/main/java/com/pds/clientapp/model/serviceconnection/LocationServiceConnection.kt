package com.pds.clientapp.model.serviceconnection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.pds.locationproviderservice.ILocationRequestInterface


class LocationServiceConnection : ServiceConnection {

    lateinit var connectionCallback: (Boolean) -> Unit
    var iRemoteInterface: ILocationRequestInterface? = null
        private set


    fun connectToService(context: Context, connectionCallback: (Boolean) -> Unit) {
        val bindingIntent: Intent = Intent().apply {
            component = ComponentName("com.pds.locationproviderservice", "com.pds.locationproviderservice.service.LocationService")
        }
        val conn = context.bindService(bindingIntent, this, Context.BIND_AUTO_CREATE)
        if (!conn)
            connectionCallback(false)
        this@LocationServiceConnection.connectionCallback = connectionCallback
    }

    fun disconnectFromService(context: Context) {
        context.unbindService(this)
        iRemoteInterface = null

    }


    override fun onServiceDisconnected(name: ComponentName?) {
        Log.e("Service Connection", "Service Disconnected")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.e("Service Connection", "Service Connected")
        iRemoteInterface = ILocationRequestInterface.Stub.asInterface(service)
        connectionCallback(true)
    }
}