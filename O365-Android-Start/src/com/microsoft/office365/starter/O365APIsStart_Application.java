/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter;

import java.util.List;
import java.util.NoSuchElementException;
import android.app.Activity;
import android.app.Application;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.discoveryservices.ServiceInfo;
import com.microsoft.discoveryservices.odata.DiscoveryClient;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.interfaces.OnSignInResultListener;
import com.microsoft.office365.starter.models.AppPreferences;
import com.microsoft.office365.starter.models.O365CalendarModel;
import com.microsoft.office365.starter.models.O365FileListModel;
import com.microsoft.office365.starter.models.O365FileModel;
import com.microsoft.services.odata.impl.DefaultDependencyResolver;

/**
 * The Class O365_APIsStart_Application.
 */
public class O365APIsStart_Application extends Application {
    private AppPreferences mPreferences;
    private Thread.UncaughtExceptionHandler mDefaultUEH;
    private boolean mUserIsAuthenticated = false;
    private O365CalendarModel mCalendarModel = null;
    private O365FileListModel fileListViewState;
    private O365FileModel displayedFile;
    private ArrayAdapter<O365FileModel> fileAdapterList;
    private List<ServiceInfo> mServices;
    private com.microsoft.sharepointservices.odata.SharePointClient fileClient;
    private OnSignInResultListener mOnSignInResultListener;

    public O365FileListModel getFileListViewState()
    {
        return fileListViewState;
    }

    public void setFileListViewState(O365FileListModel value)
    {
        fileListViewState = value;
    }

    public O365FileModel getDisplayedFile()
    {
        return displayedFile;
    }

    public void setDisplayedFile(O365FileModel value)
    {
        displayedFile = value;
    }

    public ArrayAdapter<O365FileModel> getFileAdapterList()
    {
        return fileAdapterList;
    }

    public void setFileAdapterList(ArrayAdapter<O365FileModel> value)
    {
        fileAdapterList = value;
    }

    public O365CalendarModel getCalendarModel()
    {
        return mCalendarModel;
    }

    public void setCalendarModel(O365CalendarModel calendarModel)
    {
        mCalendarModel = calendarModel;
    }

    public boolean userIsAuthenticated() {
        return mUserIsAuthenticated;
    }

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("Client", "UncaughtException", ex);
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };

    public void setOnSignInResultListener(OnSignInResultListener listener)
    {
        mOnSignInResultListener = listener;
    }

    public void discoverServices(final Activity currentActivity) {
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) Controller
                .getInstance().getDependencyResolver();
        DiscoveryClient discoveryClient = new DiscoveryClient(Constants.DISCOVERY_RESOURCE_URL,
                dependencyResolver);

        try {
            ListenableFuture<List<ServiceInfo>> services = discoveryClient.getservices().read();
            Futures.addCallback(services, new FutureCallback<List<ServiceInfo>>() {
                @Override
                public void onSuccess(final List<ServiceInfo> result)
                {
                    mUserIsAuthenticated = true;
                    mServices = result;
                    final OnSignInResultListener.Event event = new OnSignInResultListener.Event();
                    event.setUserSignInStatus(true);
                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnSignInResultListener.onSignInResultEvent(event);

                        }
                    });
                }

                @Override
                public void onFailure(final Throwable t)
                {
                    Log.e("Asset", t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc) /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {

        super.onCreate();
        mPreferences = new AppPreferences(
                PreferenceManager.getDefaultSharedPreferences(this));

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public AppPreferences getAppPreferences() {
        return mPreferences;
    }

    public boolean hasConfiguration() {

        if (TextUtils.isEmpty(mPreferences.getClientId()))
            return false;

        if (TextUtils.isEmpty(mPreferences.getRedirectUrl()))
            return false;

        return true;
    }



    /**
     * Handle error.
     * 
     * @param throwable the throwable
     */
    public void handleError(Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
        Log.e("Asset", throwable.toString());
    }

    /**
     * Clear preferences.
     */
    public void clearPreferences() {
        CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
        if (syncManager != null) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
    }

    public List<ServiceInfo> getServices() {
        if (mServices != null)
            return mServices;
        else
            throw new NullPointerException(
                    "The Office 365 services have not been discovered yet. Try calling the discoverServices method first.");
    }

    public ServiceInfo getService(String capability) {
        if (mServices == null)
            return null;
        for (ServiceInfo service : mServices)
            if (service.getcapability().equals(capability))
                return service;

        throw new NoSuchElementException("The Office 365 capability " + capability
                + "was not found in services. Current capabilities are 'MyFiles' and 'Calendar'");
    }

    /**
     * Gets the current list client.
     * 
     * @return the current list client
     */
    public com.microsoft.sharepointservices.odata.SharePointClient getFileClient()
    {
        if (fileClient != null)
            return fileClient;
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) Controller
                .getInstance().getDependencyResolver();
        ServiceInfo di = getService(Constants.MYFILES_CAPABILITY);
        // TODO add proper check for null in just one spot
        String serviceEndpointUri = di.getserviceEndpointUri();
        Controller.getInstance().setDependencyResolver(dependencyResolver);
        fileClient = new com.microsoft.sharepointservices.odata.SharePointClient(
                serviceEndpointUri, dependencyResolver);

        return fileClient;
    }

    
    //This method should get and cache the client. Returned the cached client. It should be good for
    //the life of the app.
    public com.microsoft.outlookservices.odata.OutlookClient getCalendarClient() {
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) Controller
                .getInstance().getDependencyResolver();
        ServiceInfo di = getService(Constants.CALENDAR_CAPABILITY);
        String serviceEndpointUri = di.getserviceEndpointUri();
        Controller.getInstance().setDependencyResolver(dependencyResolver);
        com.microsoft.outlookservices.odata.OutlookClient client = new com.microsoft.outlookservices.odata.OutlookClient(
                serviceEndpointUri, dependencyResolver);
        return client;
    }

}

//*********************************************************
//
//O365-Android-Start, https://github.com/OfficeDev/O365-Android-Start
//
//Copyright (c) Microsoft Corporation
//All rights reserved.
//
//MIT License:
//Permission is hereby granted, free of charge, to any person obtaining
//a copy of this software and associated documentation files (the
//"Software"), to deal in the Software without restriction, including
//without limitation the rights to use, copy, modify, merge, publish,
//distribute, sublicense, and/or sell copies of the Software, and to
//permit persons to whom the Software is furnished to do so, subject to
//the following conditions: 
//
//The above copyright notice and this permission notice shall be
//included in all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
//OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
//WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//*********************************************************