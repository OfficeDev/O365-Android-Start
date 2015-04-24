/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter;

import java.util.List;
import java.util.NoSuchElementException;
import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.discoveryservices.ServiceInfo;
import com.microsoft.discoveryservices.odata.DiscoveryClient;
import com.microsoft.office365.starter.helpers.AuthenticationController;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.interfaces.OnServicesDiscoveredListener;
import com.microsoft.office365.starter.Calendar.O365CalendarModel;
import com.microsoft.office365.starter.FilesFolders.O365FileListModel;
import com.microsoft.office365.starter.FilesFolders.O365FileModel;
import com.microsoft.office365.starter.Email.O365MailItemsModel;
import com.microsoft.services.odata.impl.DefaultDependencyResolver;
import com.microsoft.fileservices.odata.SharePointClient;
import com.microsoft.outlookservices.odata.OutlookClient;

public class O365APIsStart_Application extends Application {
	private Thread.UncaughtExceptionHandler mDefaultUEH;
	private boolean mUserIsAuthenticated = false;
	private O365CalendarModel mCalendarModel = null;

	private O365FileListModel mFileListViewState;
	private O365FileModel mDisplayedFile;
    private O365MailItemsModel mMailItemsModel;
    private ArrayAdapter<O365FileModel> mFileAdapterList;
	private List<ServiceInfo> mServices;
	private SharePointClient mFileClient;
	private OutlookClient mCalendarClient;
    private OutlookClient mMailClient;
    private OnServicesDiscoveredListener mOnServicesDiscoveredResultListener;


    public O365MailItemsModel getMailItemsModel()
    {
        return mMailItemsModel;
    }

	public O365FileListModel getFileListViewState() {
		return mFileListViewState;
	}

	public void setFileListViewState(O365FileListModel value) {
		mFileListViewState = value;
	}

	public O365FileModel getDisplayedFile() {
		return mDisplayedFile;
	}

	public void setDisplayedFile(O365FileModel value) {
		mDisplayedFile = value;
	}

	public ArrayAdapter<O365FileModel> getFileAdapterList() {
		return mFileAdapterList;
	}

	public void setFileAdapterList(ArrayAdapter<O365FileModel> value) {
		mFileAdapterList = value;
	}

	public O365CalendarModel getCalendarModel() {
		return mCalendarModel;
	}

	public void setCalendarModel(O365CalendarModel calendarModel) {
		mCalendarModel = calendarModel;
	}

    public void setMailItemsModel(O365MailItemsModel mailItemsModel){
        mMailItemsModel = mailItemsModel;
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

	public void setOnServicesDiscoveredResultListener(OnServicesDiscoveredListener listener) {
		mOnServicesDiscoveredResultListener = listener;
	}

	public void discoverServices(final Activity currentActivity) {
        AuthenticationController.getInstance().setResourceId(Constants.DISCOVERY_RESOURCE_ID);
		DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) AuthenticationController
				.getInstance().getDependencyResolver();
		DiscoveryClient discoveryClient = new DiscoveryClient(Constants.DISCOVERY_RESOURCE_URL, dependencyResolver);

		try {
			ListenableFuture<List<ServiceInfo>> services = discoveryClient
					.getservices().read();
			Futures.addCallback(services,
					new FutureCallback<List<ServiceInfo>>() {
						@Override
						public void onSuccess(final List<ServiceInfo> result) {
							mUserIsAuthenticated = true;
							mServices = result;
							final OnServicesDiscoveredListener.Event event = new OnServicesDiscoveredListener.Event();
							event.setServicesAreDiscovered(true);
							currentActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mOnServicesDiscoveredResultListener
											.onServicesDiscoveredEvent(event);
								}
							});
						}

						@Override
						public void onFailure(final Throwable t) {
							Log.e("Asset", t.getMessage());
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}


	public void clearCookies() {
		CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
		if (syncManager != null) {
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			CookieSyncManager.getInstance().sync();
		}
	}

	public void clearClientObjects() {
		mFileClient = null;
		mCalendarClient = null;
        mMailClient = null;
	}

    public void clearTokens(){
        if (AuthenticationController.getInstance().getAuthenticationContext() != null)
        {
            AuthenticationController.getInstance().getAuthenticationContext().getCache().removeAll();
        }
    }

	private ServiceInfo getService(String capability) {
		if (mServices == null)
			return null;
		for (ServiceInfo service : mServices)
			if (service.getcapability().equals(capability))
				return service;

		throw new NoSuchElementException(
				"The Office 365 capability "
						+ capability
						+ "was not found in services. Current capabilities are 'MyFiles', 'Calendar', and 'Mail'");
	}

	/**
	 * Gets the current list client.
	 *
	 * @return the current list client
	 */
	public SharePointClient getFileClient() {
		if (mFileClient != null)
			return mFileClient;

        ServiceInfo discoveryInfo = getService(Constants.MYFILES_CAPABILITY);
        String serviceEndpointUri = discoveryInfo.getserviceEndpointUri();
        String serviceResourceId = discoveryInfo.getserviceResourceId();

        AuthenticationController.getInstance().setResourceId(serviceResourceId);
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) AuthenticationController.getInstance().getDependencyResolver();

		mFileClient = new SharePointClient(serviceEndpointUri, dependencyResolver);
		return mFileClient;
	}

	// This method should get and cache the client. Returned the cached client.
	// It should be good for the life of the app.
	public OutlookClient getCalendarClient() {
		if (mCalendarClient != null)
			return mCalendarClient;

		ServiceInfo discoveryInfo = getService(Constants.CALENDAR_CAPABILITY);
		String serviceEndpointUri = discoveryInfo.getserviceEndpointUri();
        String serviceResourceId = discoveryInfo.getserviceResourceId();

        AuthenticationController.getInstance().setResourceId(serviceResourceId);
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) AuthenticationController.getInstance().getDependencyResolver();

		mCalendarClient = new OutlookClient(serviceEndpointUri, dependencyResolver);
		return mCalendarClient;
	}

    // This method should get and cache the client. Returned the cached client.
    // It should be good for the life of the app.
    public OutlookClient getMailClient() {
        if (mMailClient != null)
            return mMailClient;

        ServiceInfo discoveryInfo = getService(Constants.MAIL_CAPABILITY);
        String serviceEndpointUri = discoveryInfo.getserviceEndpointUri();
        String serviceResourceId = discoveryInfo.getserviceResourceId();

        AuthenticationController.getInstance().setResourceId(serviceResourceId);
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) AuthenticationController.getInstance().getDependencyResolver();

        mMailClient = new OutlookClient(serviceEndpointUri, dependencyResolver);
        return mMailClient;
    }
}

// *********************************************************
//
// O365-Android-Start, https://github.com/OfficeDev/O365-Android-Start
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************
