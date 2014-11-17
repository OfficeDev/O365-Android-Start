/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.helpers;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationSettings;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.aad.adal.UserInfo;
import com.microsoft.services.odata.impl.DefaultDependencyResolver;
import com.microsoft.services.odata.interfaces.Credentials;
import com.microsoft.services.odata.interfaces.CredentialsFactory;
import com.microsoft.services.odata.interfaces.Request;
import com.microsoft.office365.starter.models.AppPreferences;
import java.util.Random;

public class Authentication {

    public static AuthenticationContext context = null;
    private static String mLoggedInUser;

    public static SettableFuture<Void> authenticate(final Activity rootActivity,
            final DefaultDependencyResolver resolver, String resourceId, AppPreferences preferences) {

        final SettableFuture<Void> result = SettableFuture.create();

        getAuthenticationContext(rootActivity)
                .acquireToken(rootActivity, resourceId,
                        Constants.CLIENT_ID.trim(), Constants.REDIRECT_URI.trim(), PromptBehavior.Auto,
                        new AuthenticationCallback<AuthenticationResult>() {

                            @Override
                            public void onSuccess(final AuthenticationResult authenticationResult) {
                                if (authenticationResult != null
                                        && !TextUtils.isEmpty(authenticationResult.getAccessToken())) {
                                    resolver.setCredentialsFactory(new CredentialsFactory() {
                                        @Override
                                        public Credentials getCredentials() {
                                            return new Credentials() {
                                                @Override
                                                public void prepareRequest(Request request) {
                                                    request.addHeader("Authorization", "Bearer "
                                                            + authenticationResult.getAccessToken());
                                                }
                                            };
                                        }
                                    });
                                    storeUserId(rootActivity, authenticationResult);
                                    result.set(null);
                                }
                            }

                            private void storeUserId(final Activity rootActivity,
                                    final AuthenticationResult authenticationResult) {

                                UserInfo ui = authenticationResult.getUserInfo();
                                SharedPreferences sharedPref = rootActivity
                                        .getPreferences(Context.MODE_PRIVATE);

                                if (ui != null) {
                                    mLoggedInUser = ui.getUserId();
                                    Editor editor = sharedPref.edit();
                                    editor.putString("UserId", mLoggedInUser);
                                    editor.putString("DisplayName", ui.getGivenName() + " " + ui.getFamilyName());
                                    editor.commit();
                                }
                                else {
                                    mLoggedInUser = sharedPref.getString("UserId", "");
                                }
                            }

                            @Override
                            public void onError(Exception exc) {
                                result.setException(exc);
                            }
                        });
        return result;
    }

    /**
     * Gets AuthenticationContext for AAD.
     * 
     * @return authenticationContext, if successful
     */
    public static AuthenticationContext getAuthenticationContext(Activity activity) {
        try {
            context = new AuthenticationContext(activity, Constants.AUTHORITY_URL, false);

        } catch (Throwable t) {
            Log.e("SampleApp", t.toString());
        }
        return context;
    }

    public static void resetToken(Activity activity) {
        getAuthenticationContext(activity).getCache().removeAll();
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