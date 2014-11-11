/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.office365.starter.Controller;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.helpers.Authentication;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.interfaces.MainActivityCoordinator;
import com.microsoft.office365.starter.interfaces.OnSignInResultListener;
import com.microsoft.office365.starter.models.AppPreferences;
import com.microsoft.office365.starter.models.O365FileModel;
import com.microsoft.services.odata.impl.DefaultDependencyResolver;

public class MainActivity extends Activity implements MainActivityCoordinator,
        OnSignInResultListener {

    private AppPreferences mAppPreferences;
    private O365APIsStart_Application mApplication;
    private MainButtonsFragment mButtonsFragment;
    private Menu mMenu;
    private OnSignInResultListener mSignInResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Authentication.createEncryptionKey(getApplicationContext());
        mApplication = (O365APIsStart_Application) getApplication();
        mApplication.setOnSignInResultListener(this);
        mAppPreferences = (mApplication).getAppPreferences();

        // When the app starts, the buttons should be disabled until the user signs in to Office 365
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
            MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);
            signInMenuItem.setIcon(R.drawable.user_default_signedin);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            switch (item.getItemId()) {
                case R.id.menu_clear_credentials:
                    ClearCredentials();
                    return true;
                case R.id.menu_preferences:
                    startActivity(new Intent(MainActivity.this,
                            AppPreferencesActivity.class));
                    return true;
                case R.id.menu_signin:
                    signIn_OnClick();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }

        } catch (Throwable t) {
            Log.e("Asset", t.getMessage());
        }
        return true;
    }

    private void ClearCredentials() {
        CookieSyncManager syncManager = CookieSyncManager
                .createInstance(getApplicationContext());

        if (syncManager != null) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
            Authentication.resetToken(this);

            userSignedOut();
        }
    }

    protected void userSignedOut() {
        mButtonsFragment.setButtonsEnabled(false);

        MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);
        signInMenuItem.setIcon(R.drawable.user_signedout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Authentication.context.onActivityResult(requestCode, resultCode, data);
    }

    public void signIn_OnClick() {
        Controller.getInstance().setDependencyResolver(new DefaultDependencyResolver());
        DefaultDependencyResolver dependencyResolver = (DefaultDependencyResolver) Controller
                .getInstance().getDependencyResolver();
        ListenableFuture<Void> future = Authentication
                .authenticate(this, dependencyResolver, Constants.DISCOVERY_RESOURCE_ID,
                        mAppPreferences);

        Futures.addCallback(future,
                new FutureCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Asset", t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        mApplication.discoverServices(MainActivity.this);

                    }
                });
    }

    @Override
    public void onServiceAuthenticated(String capability) {
        Intent intentToActivate = null;
        if (capability.equals(Constants.MYFILES_CAPABILITY)) {
            intentToActivate = new Intent(this, FilesActivity.class);
        }
        if (capability.equals(Constants.CALENDAR_CAPABILITY)) {
            intentToActivate = new Intent(this, CalendarEventListActivity.class);
        }

        startActivity(intentToActivate);
    }

    @Override
    public void onSignInResultEvent(Event event) {
        if (event.getUserSignInStatus())
        {
            // User was signed in so activate the buttons.
            mButtonsFragment.setButtonsEnabled(true);

            MenuItem signInMenuItem = mMenu.findItem(R.id.menu_signin);
            signInMenuItem.setIcon(R.drawable.user_default_signedin);
        }
        else
            Toast.makeText(this, "Error signing in", 3).show();

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