/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.models.O365CalendarModel;

import android.content.Intent;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;

/**
* An activity representing a single CalendarEvent detail screen. This activity is only used on
* handset devices. On tablet-size devices, item details are presented side-by-side with a list of
* items in a {@link CalendarEventListActivity}.
* <p>
* This activity is mostly just a 'shell' activity containing nothing more than a
* {@link CalendarEventDetailFragment}.
*/
public class CalendarEventDetailActivity extends Activity {
    public O365CalendarModel mCalendarModel;
    private O365APIsStart_Application mApplication;

    public static final String ARG_ACTION = "action";

    public enum formAction
    {
        create,
        update,
        delete,
        select
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendarevent_detail);

        mApplication = (O365APIsStart_Application) this.getApplication();
        mCalendarModel = mApplication.getCalendarModel();

        if (mCalendarModel == null)
            mCalendarModel = new O365CalendarModel(this);

        // Show the Up button in the action bar.
        ActionBar aBar = getActionBar();
        if (aBar != null)
            aBar.setDisplayHomeAsUpEnabled(true);

        String action = "select";
        Intent fromIntent = getIntent();
        if (fromIntent.getExtras().containsKey(ARG_ACTION)) {
            action = fromIntent.getStringExtra(ARG_ACTION);

        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(
                    CalendarEventDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(
                            CalendarEventDetailFragment.ARG_ITEM_ID));

            // pick the fragment to load based on user's chosen action
            switch (formAction.valueOf(action))
            {
                case create:
                    CalendarEventFragmentCreate createFragment = new CalendarEventFragmentCreate();
                    createFragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .add(R.id.calendarevent_detail_container, createFragment)
                            .commit();
                    break;
                case update:
                    CalendarEventFragmentUpdate fragment = new CalendarEventFragmentUpdate();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .add(R.id.calendarevent_detail_container, fragment)
                            .commit();

                    break;
                case delete:
                    CalendarEventDetailFragment deleteFragment = new CalendarEventDetailFragment();
                    deleteFragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .add(R.id.calendarevent_detail_container, deleteFragment)
                            .commit();
                    break;

                default:
                    try
                    {
                        CalendarEventDetailFragment defaultFragment = new CalendarEventDetailFragment();
                        defaultFragment.setArguments(arguments);
                        getFragmentManager().beginTransaction()
                                .add(R.id.detailFragmentView, defaultFragment)
                                .commit();
                    } catch (Exception ex)
                    {
                        Log.e("Exception: " + ex.getMessage(), "CalendarEventDetailActivity");
                    }
                    break;
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            navigateUpTo(new Intent(this, CalendarEventListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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