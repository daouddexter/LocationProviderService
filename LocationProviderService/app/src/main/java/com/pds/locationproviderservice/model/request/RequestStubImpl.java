package com.pds.locationproviderservice.model.request;

import android.os.RemoteException;
import com.pds.locationproviderservice.ILocationCallbackInterface;
import com.pds.locationproviderservice.ILocationRequestInterface;

public class RequestStubImpl extends ILocationRequestInterface.Stub {


    @Override
    public void registerPeriodicLocation(int interval, ILocationCallbackInterface callback) throws RemoteException {

    }

    @Override
    public void getCurrentLocation(ILocationCallbackInterface callback) throws RemoteException {

    }

    @Override
    public void unRegisterPeriodicLocation(ILocationCallbackInterface callback) throws RemoteException {

    }
}