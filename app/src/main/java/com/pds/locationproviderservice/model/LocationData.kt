package com.pds.locationproviderservice.model

import android.os.Parcel
import android.os.Parcelable


class LocationData() : Parcelable {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(parcel: Parcel) : this() {
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    public fun readFromParcel(inParcel: Parcel) {
        latitude = inParcel.readDouble()
        longitude = inParcel.readDouble()

    }

    companion object CREATOR : Parcelable.Creator<LocationData> {
        override fun createFromParcel(parcel: Parcel): LocationData {
            return LocationData(parcel)
        }

        override fun newArray(size: Int): Array<LocationData?> {
            return arrayOfNulls(size)
        }
    }


}