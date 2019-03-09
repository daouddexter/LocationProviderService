package com.pds.locationproviderservice.infrastructure

import android.content.Context
import android.location.Location
import com.pds.locationproviderservice.constants.Constants
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

interface ILocationHandler {

    fun registerForLocationUpdate(
        context: Context,
        interval: Long = 10000, forPeriodic: Boolean
    ): Single<Constants.LocationRegistrationResults>

    fun unregisterFromLocationUpdate()
    fun getLocationSubject(): BehaviorSubject<Location>
    fun getCurrentLocation(context: Context): Single<Location?>

}