/*******************************************************************************
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.

 ******************************************************************************/
package com.microsoft.office365.starter.helpers;

import android.app.Activity;
import android.widget.Toast;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * holds the context of execution across activities
 */
public class AsyncController {

    private static AsyncController INSTANCE;

    ExecutorService executor;

    private AsyncController() {
        this.executor = Executors.newFixedThreadPool(2);
    }

    /**
     * Creates a singleton instance of the AsyncController
     * @return an instance of AsyncController class
     */
    public static synchronized AsyncController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AsyncController();
        }

        return INSTANCE;
    }



    /**
     * post an async task to the executor thread pool
     * @param callable the task to be executed
     *
     */
    public <V> void postAsyncTask(Callable<V> callable) {
        this.executor.submit(callable);
    }

    /**
     * notifies about the exception on executing the Future
     *
     * @param msg error message to be displayed
     */
    public void handleError(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}


