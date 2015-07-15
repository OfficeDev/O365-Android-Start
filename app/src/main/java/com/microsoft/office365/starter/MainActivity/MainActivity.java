/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.MainActivity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.office365.starter.Calendar.CalendarEventListActivity;
import com.microsoft.office365.starter.Email.MailItemListActivity;
import com.microsoft.office365.starter.FilesFolders.FileListActivity;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.helpers.AuthenticationController;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.helpers.AsyncController;
import com.microsoft.office365.starter.helpers.ProgressDialogHelper;
import com.microsoft.office365.starter.interfaces.MainActivityCoordinator;
import com.microsoft.office365.starter.interfaces.OnServicesDiscoveredListener;

import java.util.concurrent.Callable;

import java.net.URI;
import java.util.UUID;

public class MainActivity extends Activity implements MainActivityCoordinator,
        OnServicesDiscoveredListener {

	private O365APIsStart_Application mApplication;
	private MainButtonsFragment mButtonsFragment;
	private Menu mMenu;
    private ProgressDialog mDialogSignIn;
    private ProgressDialog mDialogDiscoverServices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mApplication = (O365APIsStart_Application) getApplication();
		mApplication.setOnServicesDiscoveredResultListener(this);

		// When the app starts, the buttons should be disabled until the user
		// signs in to Office 365
		FragmentManager fragmentManager = getFragmentManager();
		mButtonsFragment = (MainButtonsFragment) fragmentManager
				.findFragmentById(R.id.buttonsFragment);
		mButtonsFragment.setButtonsEnabled(mApplication.userIsAuthenticated());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu = menu;

		if (mApplication.userIsAuthenticated()) {
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            String displayName = sharedPref.getString("DisplayName", "");

			MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);
			signInMenuItem.setIcon(R.drawable.user_default_signedin);
            signInMenuItem.setTitle(displayName);
        }

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		try {
			switch (item.getItemId()) {
			case R.id.menu_clear_credentials:
				clearCredentials();
				return true;
			case R.id.menu_signin:
                //check that client id and redirect have been set correctly
                try
                {
                    UUID.fromString(Constants.CLIENT_ID);
                    URI.create(Constants.REDIRECT_URI);
                }
                catch (IllegalArgumentException e)
                {
                    Toast.makeText(
                            this
                            , getString(R.string.warning_clientid_redirecturi_incorrect)
                            , Toast.LENGTH_LONG).show();
                    return true;
                }

				signIn_OnClick();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}

		} catch (Throwable t) {
            if (t.getMessage() == null)
			    Log.e("Asset", " ");
            else
                Log.e("Asset", t.getMessage());
		}
		return true;
	}

	private void clearCredentials() {
		mApplication.clearClientObjects();
		mApplication.clearCookies();
        mApplication.clearTokens();
		userSignedOut();
	}

	protected void userSignedOut() {
		mButtonsFragment.setButtonsEnabled(false);

		MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);
		signInMenuItem.setIcon(R.drawable.user_signedout);

		signInMenuItem.setTitle(R.string.MainActivity_SignInButtonText);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        AuthenticationController
                .getInstance()
                .getAuthenticationContext()
                .onActivityResult(
                        requestCode
                        , resultCode
                        , data);
	}

	public void signIn_OnClick() {
        mDialogSignIn = ProgressDialogHelper.showProgressDialog(
                MainActivity.this, "Authenticating to Office 365...",
                "Please wait.");

        AuthenticationController.getInstance().setContextActivity(this);
		SettableFuture<Boolean> authenticated = AuthenticationController.getInstance().initialize();

		Futures.addCallback(authenticated, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i("MainActivity", "Authentication successful");

                AsyncController.getInstance().postAsyncTask(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDialogSignIn.isShowing()) {
                                    mDialogSignIn.dismiss();
                                }
                                Toast.makeText(
                                        MainActivity.this,
                                        "Authentication successful",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        return null;
                    }
                });

                // Discover services
                mDialogDiscoverServices = ProgressDialogHelper.showProgressDialog(
                        MainActivity.this, "Discovering Services...",
                        "Please wait.");
                mApplication.discoverServices(MainActivity.this);
            }
			@Override
			public void onFailure(final Throwable t) {
                Log.e("MainActivity", t.getMessage());

                AsyncController.getInstance().postAsyncTask(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDialogSignIn.isShowing()) {
                                    mDialogSignIn.dismiss();
                                }
                                Toast.makeText(
                                        MainActivity.this,
                                        "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        return null;
                    }
                });
			}
		});
	}

	@Override
	public void onServiceSelected(String capability) {
		Intent intentToActivate = null;
		if (capability.equals(Constants.MYFILES_CAPABILITY)) {
			intentToActivate = new Intent(this, FileListActivity.class);
		}
		if (capability.equals(Constants.CALENDAR_CAPABILITY)) {
			intentToActivate = new Intent(this, CalendarEventListActivity.class);
		}
        if (capability.equals(Constants.MAIL_CAPABILITY)) {
            intentToActivate = new Intent(this, MailItemListActivity.class);
        }

		startActivity(intentToActivate);
	}

	@Override
	public void onServicesDiscoveredEvent(Event event) {
		if (event.servicesAreDiscovered()) {
            Log.i("MainActivity", "Services discovered");
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            final String displayName = sharedPref.getString("DisplayName", "");
            final MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);

            AsyncController.getInstance().postAsyncTask(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // User was signed in so activate the buttons.
                            mButtonsFragment.setButtonsEnabled(true);
                            signInMenuItem.setIcon(R.drawable.user_default_signedin);
                            signInMenuItem.setTitle(displayName);

                            if (mDialogDiscoverServices.isShowing()) {
                                mDialogDiscoverServices.dismiss();
                            }
                            Toast.makeText(
                                    MainActivity.this,
                                    "Services discovered",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    return null;
                }
            });
		} else{
            Log.e("MainActivity", "Failed to discover services");
            AsyncController.getInstance().postAsyncTask(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mDialogDiscoverServices.isShowing()) {
                                mDialogDiscoverServices.dismiss();
                            }
                            Toast.makeText(
                                    MainActivity.this,
                                    "Failed to discover services",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    return null;
                }
            });
        }
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
