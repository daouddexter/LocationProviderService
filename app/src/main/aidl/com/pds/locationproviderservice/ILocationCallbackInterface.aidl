// ILocationCallbackInterface.aidl
package com.pds.locationproviderservice;

import android.location.Location;

interface ILocationCallbackInterface {

    void onCurrentLocation(in Location location);
    void onPeriodicLocation(in Location location);
    int getPid();
}
