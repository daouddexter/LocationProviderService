package com.pds.clientapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pds.clientapp.R
import com.pds.clientapp.model.CallbackStubImpl
import com.pds.clientapp.model.serviceconnection.LocationServiceConnection
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val callbackStubImpl: CallbackStubImpl by lazy {
        CallbackStubImpl()
    }

    private var connection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val locationServiceConnection = LocationServiceConnection()
        locationServiceConnection.connectToService(this) { connection ->
            this@MainActivity.connection = connection

        }
        connect_to_service.setOnClickListener {
            if (connection)
                listenToLocation(locationServiceConnection)
        }

    }

    private fun listenToLocation(locationServiceConnection: LocationServiceConnection) {
        locationServiceConnection.iRemoteInterface?.registerPeriodicLocation(3000, callbackStubImpl)

    }
}
