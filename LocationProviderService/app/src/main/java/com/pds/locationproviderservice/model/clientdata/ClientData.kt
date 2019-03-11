package com.pds.locationproviderservice.model.clientdata

import com.pds.locationproviderservice.ILocationCallbackInterface
import io.reactivex.disposables.Disposable

data class ClientData(val packageName: String, val callback: ILocationCallbackInterface?, val disposable: Disposable)