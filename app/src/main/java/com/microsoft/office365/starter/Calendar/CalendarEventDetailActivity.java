/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Calendar;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.Calendar.O365CalendarModel.O365Calendar_Event;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.helpers.DeleteDialogFragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * This activity is created when the device is in small screen mode. It is NOT used when the device
 * is a tablet of 7" or larger. An activity representing a single CalendarEvent detail screen. This
 * activity is only used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link CalendarEventListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a
 * {@link CalendarEventDetailFragment}.
 */
public class CalendarEventDetailActivity extends Activity implements NoticeDialogListener,
        OnOperationCompleteListener,
        View.OnClickListener
{
    public O365CalendarModel mCalendarModel;
    private O365APIsStart_Application mApplication;
    private DeleteDialogFragment mDeleteDialog;
    private ProgressDialog mDialog;
    private O365CalendarModel.O365Calendar_Event mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendarevent_detail);
        mApplication = (O365APIsStart_Application) this.getApplication();
        mCalendarModel = mApplication.getCalendarModel();

        // Show the Up button in the action bar.
        ActionBar aBar = getActionBar();
        if (aBar != null)
            aBar.setDisplayHomeAsUpEnabled(true);

        mCalendarModel = ((O365APIsStart_Application) getApplication()).getCalendarModel();

        if (savedInstanceState == null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            if (getIntent()
                    .getAction()
                    .equals(Intent.ACTION_INSERT))
            {
                CalendarEventFragmentView createFragment = new CalendarEventFragmentView();
                createFragment.setArguments(arguments);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.calendarevent_detail_container, createFragment)
                        .commit();
            }
            else if (getIntent()
                    .getAction()
                    .equals(Intent.ACTION_EDIT))
            {
                // Get the calendar event to update
                mItem = mCalendarModel
                        .getCalendar()
                        .ITEM_MAP
                                .get(getIntent().getStringExtra(
                                        CalendarEventDetailFragment.ARG_ITEM_ID));

                // Pass the id of the event to the view fragment that will be opened
                arguments.putString(
                        CalendarEventDetailFragment.ARG_ITEM_ID,
                        getIntent().getStringExtra(
                                CalendarEventDetailFragment.ARG_ITEM_ID));

                CalendarEventFragmentView fragment = new CalendarEventFragmentView();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .add(R.id.calendarevent_detail_container, fragment)
                        .commit();
            }
            else if (getIntent()
                    .getAction()
                    .equals(Intent.ACTION_DELETE))
            {
                CalendarEventDetailFragment deleteFragment = new CalendarEventDetailFragment();
                deleteFragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .add(R.id.calendarevent_detail_container, deleteFragment)
                        .commit();
            }
            else
            {
                CalendarEventDetailFragment deleteFragment = new CalendarEventDetailFragment();
                deleteFragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .add(R.id.calendarevent_detail_container, deleteFragment)
                        .commit();

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

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDialogPositiveClick(Fragment dialog) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        if (dialog != mDeleteDialog)
        {
            mDialog = new ProgressDialog(this);
            mDialog.setTitle("Updating an event...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(true);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mCalendarModel.setEventOperationCompleteListener(this);
            mCalendarModel.postUpdatedEvent(this, mItem);
        }
    }

    @Override
    public void onDialogPositiveClick(Fragment dialog, O365Calendar_Event editedEvent,
            boolean newItemFlag)
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // post the event to server based on formAction
        mDialog = new ProgressDialog(this);
        if (newItemFlag)
            mDialog.setTitle("Adding an event...");
        else
            mDialog.setTitle("Updating an event...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(true);
        mDialog.setIndeterminate(true);
        mDialog.show();

        // Register callback with the model for notification of op complete
        mCalendarModel.setEventOperationCompleteListener(this);
        if (newItemFlag)
            mCalendarModel.postCreatedEvent(this, editedEvent);
        else
            mCalendarModel.postUpdatedEvent(this, editedEvent);
    }

    @Override
    public void onDialogNegativeClick(Fragment dialog) {
        this.finish();
    }

    @Override
    public void onOperationComplete(final OperationResult opResult) {
        this.runOnUiThread(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                }

                Toast.makeText(CalendarEventDetailActivity.this, opResult.getOperationResult(),
                        Toast.LENGTH_LONG).show();
                CalendarEventListFragment calenderListFragment = (CalendarEventListFragment) getFragmentManager()
                        .findFragmentById(R.id.calendarevent_list);
            }
        });
        this.finish();
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
