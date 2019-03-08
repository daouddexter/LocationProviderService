// ILocationRequestInterface.aidl
package com.pds.locationproviderservice;

import com.pds.locationproviderservice.ILocationCallbackInterface;

interface ILocationRequestInterface {


   void registerPeriodicLocation(int interval, in ILocationCallbackInterface callback);
   void getCurrentLocation(in ILocationCallbackInterface callback);
   void unRegisterPeriodicLocation(in ILocationCallbackInterface callback);
}
