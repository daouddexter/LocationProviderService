package com.pds.locationproviderservice.model.request

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.location.Location
import android.os.Binder
import android.os.RemoteException
import android.util.Log
import com.pds.locationproviderservice.ILocationCallbackInterface
import com.pds.locationproviderservice.ILocationRequestInterface
import com.pds.locationproviderservice.constants.Constants
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import com.pds.locationproviderservice.model.clientdata.ClientData
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class RequestStubImpl(private val context: Context, private val locationHandler: ILocationHandler) :
    ILocationRequestInterface.Stub() {
    private val clientDataList: MutableList<ClientData> = ArrayList()
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var mRegistered = false


    override fun registerPeriodicLocation(interval: Int, callback: ILocationCallbackInterface?) {
        if (interval < 2000) {
            throw RemoteException("Interval should be greater than or equal to 2000ms")

        }
        val pkgName = packageName()
        pkgName?.let { packageName ->
            if (!clientDataList.stream().anyMatch { clientData ->
                    clientData?.packageName == packageName
                }) {

                synchronized(RequestStubImpl::class) {
                    Completable.create {
                        callback?.run {
                            if (mRegistered)
                                periodicObserver(interval, this, packageName)
                            else {
                                registerLocationData().subscribe({
                                    if (it) {
                                        periodicObserver(interval, this, packageName)
                                    } else throw RemoteException("Unable to register to location service")
                                }, {
                                    throw RemoteException(it.message)
                                })
                            }

                        }


                    }
                }.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            }
        }


    }

    override fun getCurrentLocation(callback: ILocationCallbackInterface?) {
        lateinit var disposible: Disposable
        disposible = locationHandler.getCurrentLocation(context).subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe({ location ->
                location?.run {
                    callback?.onCurrentLocation(this)
                }
                disposible.dispose()
            }, {
                disposible.dispose()
                throw RemoteException(it.message)
            })
    }

    override fun unRegisterPeriodicLocation(callback: ILocationCallbackInterface?) {

        packageName().run {
            clientDataList.stream().filter { clientData ->
                clientData?.packageName == this
            }.forEach { clientData ->
                clientData.disposable.dispose()
            }
            clientDataList.removeAll { clientData ->
                clientData.packageName == this
            }

        }






        if (clientDataList.isEmpty())
            unRegisterLocationData()
    }


    private fun registerLocationData(): Single<Boolean> =
        Single.defer<Boolean> {
            Single.create<Boolean> { emitter ->
                synchronized(RequestStubImpl::class) {
                    disposable.add(
                        locationHandler.registerForLocationUpdate(context, 2000, true)
                            .subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                                {
                                    mRegistered = it == Constants.LocationRegistrationResults.SUCCESS
                                    emitter.onSuccess(mRegistered)
                                },
                                {
                                    mRegistered = false
                                    emitter.onSuccess(false)
                                })
                    )

                }
            }
        }

    private fun periodicObserver(
        interval: Int,
        callback: ILocationCallbackInterface,
        pkgName: String
    ) {

        val clientData = ClientData(
            disposable = locationHandler.getLocationSubject().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .toFlowable(BackpressureStrategy.LATEST).concatMap { location ->
                    return@concatMap Flowable.just<Location>(location).delay(interval.toLong(), TimeUnit.MILLISECONDS)
                }.subscribe({
                    callback.onPeriodicLocation(it)
                }, {
                    throw RemoteException(it.message)
                }), callback = callback, packageName = pkgName
        )
        clientDataList.add(clientData)


    }

    private fun unRegisterLocationData() {
        synchronized(RequestStubImpl::class) {
            disposable.clear()
            locationHandler.unregisterFromLocationUpdate()
            mRegistered = false
        }

    }

    fun reset() {
        if (clientDataList.isNotEmpty()) {
            clientDataList.forEach {
                it.disposable.dispose()

            }
            clientDataList.clear()
            unRegisterLocationData()

        }


    }

    private fun packageName(): String? {
        var pkgName: String? = null
        val pid = Binder.getCallingPid()

        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val processes = am!!.runningAppProcesses

        for (proc in processes) {

            if (proc.pid == pid) {

                pkgName = proc.processName
            }
        }
        // package name of calling application package
        Log.e("Package Name", pkgName)
        return pkgName
    }
}