package com.pds.locationproviderservice.model.request

import android.content.Context
import android.location.Location
import android.os.RemoteException
import com.pds.locationproviderservice.ILocationCallbackInterface
import com.pds.locationproviderservice.ILocationRequestInterface
import com.pds.locationproviderservice.constants.Constants
import com.pds.locationproviderservice.infrastructure.ILocationHandler
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RequestStubImpl(private val context: Context, private val locationHandler: ILocationHandler) : ILocationRequestInterface.Stub() {
    private val disposableMap: MutableMap<ILocationCallbackInterface?, Disposable> = HashMap()
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var mRegistered = false


    override fun registerPeriodicLocation(interval: Int, callback: ILocationCallbackInterface?) {
        if (interval < 2000) {
            throw RemoteException("Interval should be greater than or equal to 2000ms")

        }
        if (!disposableMap.containsKey(callback)) {

            callback?.run {
                if(mRegistered)
                    periodicObserver(interval, this)
                else{
                    registerLocationData().subscribe({
                        if(it){
                            periodicObserver(interval, this)
                        }else  throw RemoteException("Unable to register to location service")
                    },{
                        throw RemoteException(it.message)
                    })
                }

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
        callback?.run {

            if (disposableMap.contains(this))
                disposableMap.remove(this)?.dispose()

        }


        if (disposableMap.isEmpty())
            unRegisterLocationData()
    }


    private fun registerLocationData(): Single<Boolean> =
        Single.defer<Boolean> {
            Single.create<Boolean> {
                synchronized(RequestStubImpl::class) {
                    disposable.add(
                        locationHandler.registerForLocationUpdate(context, 2000, true)
                            .subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                                {
                                    mRegistered = it == Constants.LocationRegistrationResults.SUCCESS
                                },
                                {
                                    mRegistered = false
                                })
                    )

                }
            }
        }

    private fun periodicObserver(interval: Int, callback: ILocationCallbackInterface) {
        disposableMap[callback] = locationHandler.getLocationSubject().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .toFlowable(BackpressureStrategy.LATEST).concatMap { location ->
                return@concatMap Flowable.just<Location>(location).delay(interval.toLong(), TimeUnit.MILLISECONDS)
            }.subscribe({
                callback.onPeriodicLocation(it)
            }, {
                throw RemoteException(it.message)
            })
    }

    private fun unRegisterLocationData() {
        synchronized(RequestStubImpl::class) {
            disposable.clear()
            locationHandler.unregisterFromLocationUpdate()
            mRegistered=false
        }

    }

    fun reset() {
        if (disposableMap.isNotEmpty()) {
            disposableMap.forEach {
                it.value.dispose()

            }
            disposableMap.clear()
            unRegisterLocationData()

        }
    }
}