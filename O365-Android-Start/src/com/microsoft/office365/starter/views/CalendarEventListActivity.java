/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.interfaces.OnEventsAddedListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.models.O365CalendarModel;
import com.microsoft.office365.starter.models.O365CalendarModel.O365Calendar_Event;
import com.microsoft.office365.starter.views.CalendarEventListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.FragmentManager.OnBackStackChangedListener;

/**
 * An activity representing a list of CalendarEvents. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link CalendarEventDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CalendarEventListFragment} and the item details (if present) is a
 * {@link CalendarEventDetailFragment}.
 * <p>
 * This activity also implements the required {@link CalendarEventListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CalendarEventListActivity extends Activity implements
        CalendarEventListFragment.Callbacks
        , OnBackStackChangedListener
        , NoticeDialogListener
        , OnEventsAddedListener
        , OnOperationCompleteListener
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;
    private CalendarEventDetailFragment detailFragment;
    private CalendarEventListFragment eventListFragment;
    private String selectedEventId = "";
    public O365CalendarModel.CalendarEvents calendarEvents;
    public O365CalendarModel mCalendarModel;
    private android.app.FragmentManager mFragmentManager;
    private CalendarEventListActivity mParentActivity;
    private DeleteDialogFragment mDeleteDialog;
    private CalendarEventFragmentUpdate mUpdateDialog;

    /**
     * The dummy content this fragment is presenting.
     */
    private O365CalendarModel.O365Calendar_Event mItem;

    /** The m stored rotation. */
    private int mStoredRotation;
    private ProgressDialog mDialog;
    private O365APIsStart_Application mApplication;

    private ArrayAdapter<O365CalendarModel.O365Calendar_Event> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (O365APIsStart_Application) this.getApplication();
        mCalendarModel = mApplication.getCalendarModel();
        mParentActivity = this;

        if (mCalendarModel == null)
            mCalendarModel = new O365CalendarModel(this);

        calendarEvents = mCalendarModel.getCalendar();

        setContentView(R.layout.activity_calendarevent_list);
        if (findViewById(R.id.calendarevent_detail_container) != null) {
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            eventListFragment = (CalendarEventListFragment) getFragmentManager()
                    .findFragmentById(R.id.calendarevent_list);
            eventListFragment.setActivateOnItemClick(true);

            CalendarEventListFragment calenderListFragment = (CalendarEventListFragment) getFragmentManager()
                    .findFragmentById(R.id.calendarevent_list);

            // create new adapter and initialize with empty events collection
            mListAdapter = new ArrayAdapter<O365CalendarModel.O365Calendar_Event>(
                    CalendarEventListActivity.this,
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1, calendarEvents.ITEMS);
            calenderListFragment.setListAdapter(mListAdapter);
            calenderListFragment
                    .getListView()
                    .setBackgroundColor(getResources()
                            .getColor(R.color.ListBackground));
        }
        if (mApplication.getCalendarModel() != null && mApplication
                .getCalendarModel()
                .getCalendar()
                .ITEMS.isEmpty() == false)
        {
            CalendarEventListFragment calendarEventListFragment = (CalendarEventListFragment) getFragmentManager()
                    .findFragmentById(R.id.calendarevent_list);
            mListAdapter = new ArrayAdapter<O365CalendarModel.O365Calendar_Event>(this,
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1, mApplication.getCalendarModel().getCalendar().ITEMS);
            calendarEventListFragment.setListAdapter(mListAdapter);
        }
        mFragmentManager = getFragmentManager();

        // Load the overview text into the WebView
        WebView introView = (WebView) findViewById(R.id.CalendarStarterTextWebView);
        introView.setBackgroundColor(getResources().getColor(
                R.color.ApplicationPageBackgroundThemeBrush));
        String introHTML = getResources().getString(R.string.calendar_view_intro);
        introView.loadData(introHTML, "text/html", "UTF-8");
        introView.setVisibility(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApplication.setCalendarModel(mCalendarModel);
    }

    /**
     * Callback method from {@link CalendarEventListFragment.Callbacks} indicating that the item
     * with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (id == null)
            return;
        this.selectedEventId = id;
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getSelectedItem(id);
        }
    }

    private void getSelectedItem(String id)
    {
        Bundle arguments = new Bundle();
        arguments.putString(CalendarEventDetailFragment.ARG_ITEM_ID, this.selectedEventId);

        detailFragment = new CalendarEventDetailFragment();
        detailFragment.setArguments(arguments);
        mFragmentManager.beginTransaction()
                .replace(R.id.calendarevent_detail_container, detailFragment)
                .commit();
    }

    // Delete the selected event
    public void delete_OnClick(View view)
    {
        if (this.selectedEventId.length() == 0)
            return;

        Bundle arguments = new Bundle();
        arguments.putInt("Message", R.string.EventDeleteLabel);
        mDeleteDialog = new DeleteDialogFragment();
        mDeleteDialog.setArguments(arguments);
        mDeleteDialog.show(mFragmentManager, "Delete this event?");
    }

    // Fill the list with calendar events
    public void getEvents_OnClick(View view)
    {
        getEventList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void update_OnClick(View view)
    {
        if (this.selectedEventId.length() == 0)
            return;

        if (mTwoPane == true)
        {
            // Disable action buttons until create event action is complete
            Button updateButton = (Button) view;
            updateButton.setClickable(false);
            Button actionButton = (Button) mParentActivity
                    .findViewById(R.id.button_calendarCreateEvent);
            actionButton.setClickable(false);

            Bundle arguments = new Bundle();
            arguments.putString(CalendarEventDetailFragment.ARG_ITEM_ID, this.selectedEventId);
            CalendarEventFragmentUpdate updateFragment = new CalendarEventFragmentUpdate();
            mUpdateDialog = updateFragment;
            updateFragment.setArguments(arguments);
            android.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.calendarevent_detail_container, updateFragment, "updateFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack("updateFragment");
            ft.commit();
        }
        else
        {
            Intent detailIntent = new Intent(this,
                    CalendarEventDetailActivity.class);
            detailIntent.putExtra(
                    CalendarEventDetailActivity.ARG_ACTION,
                    CalendarEventDetailActivity.formAction.update.name());
            detailIntent.putExtra(
                    CalendarEventDetailFragment.ARG_ITEM_ID,
                    this.selectedEventId);
            startActivity(detailIntent);
        }
        // Need code that reacts to popping update fragment of of stack. The
        // required code would reload the view to show the updated fragment details.
    }

    // Create new calendar event
    public void newEvent_OnClick(View view)
    {
        createNewEvent();
    }

    // Opens create event fragment and swaps with current event detail fragment
    private void createNewEvent()
    {
        if (mTwoPane == true)
        {
            // Disable action buttons until create event action is complete
            Button actionButton = (Button) mParentActivity
                    .findViewById(R.id.button_calendarUpdateEvent);
            actionButton.setClickable(false);
            actionButton = (Button) mParentActivity.findViewById(R.id.button_calendarCreateEvent);
            actionButton.setClickable(false);

            CalendarEventFragmentCreate createFragment = new CalendarEventFragmentCreate();

            android.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(R.id.calendarevent_detail_container, createFragment, "createFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack("createFragment");
            ft.commit();
        }
        else
        {
            Intent detailIntent = new Intent(this,
                    CalendarEventDetailActivity.class);
            detailIntent.putExtra(
                    CalendarEventDetailActivity.ARG_ACTION,
                    CalendarEventDetailActivity.formAction.create.name());
            startActivity(detailIntent);
        }
    }

    @Override
    public void onBackStackChanged() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        // Save user's current state
        // savedInstanceState.put
        mApplication.setCalendarModel(mCalendarModel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar_small_list, menu);
        return true;
    }

    // Callback called by update and delete fragments when user clicks the
    // Done button on the fragments
    @Override
    public void onDialogPositiveClick(Fragment dialog) {

        mStoredRotation = this.getRequestedOrientation();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        if (dialog == mDeleteDialog)
        {
            mParentActivity = this;
            mDialog = new ProgressDialog(this);
            mDialog.setTitle("Removing an event...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mCalendarModel.setEventOperationCompleteListener(this);
            mItem = mCalendarModel.getCalendar().ITEM_MAP.get(this.selectedEventId);
            mCalendarModel.postDeletedEvent(this, mItem);
        }
        else if (dialog == mUpdateDialog)
        {
            mDialog = new ProgressDialog(this);
            mDialog.setTitle("Updating an event...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mItem = mCalendarModel.getCalendar().ITEM_MAP.get(this.selectedEventId);
            mCalendarModel.setEventOperationCompleteListener(this);
            mCalendarModel.postUpdatedEvent(this, mItem);
        }
    }

    // Callback is called when user clicks the Cancel button on the update or delete event fragments
    @Override
    public void onDialogNegativeClick(Fragment dialog)
    {
        // Restore click event in the action buttons
        Button actionButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarUpdateEvent);
        actionButton.setClickable(true);
        actionButton = (Button) mParentActivity.findViewById(R.id.button_calendarCreateEvent);
        actionButton.setClickable(true);
    }

    // Called when the user click the Get Events button on this activity
    public void getEventList()
    {
        mStoredRotation = mParentActivity.getRequestedOrientation();
        mParentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        mDialog = new ProgressDialog(mParentActivity);
        mDialog.setTitle("Retrieving Events...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(false);
        mDialog.setIndeterminate(true);
        mDialog.show();

        // Register a callback on the event model to be called
        // when events are retrieved from Outlook service
        mCalendarModel.setEventSelectionListener(this);
        mCalendarModel.getEventList();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save user's current state
        // savedInstanceState.put
        mApplication.setCalendarModel(mCalendarModel);
    }

    @Override
    public void OnEventsAdded(final setEventCollection eventCollection) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!eventCollection.getEventCollection().isEmpty())
                {
                    mListAdapter = new ArrayAdapter<O365CalendarModel.O365Calendar_Event>(
                            CalendarEventListActivity.this,
                            android.R.layout.simple_list_item_activated_1,
                            android.R.id.text1, eventCollection.getEventCollection());

                    CalendarEventListFragment calenderListFragment = (CalendarEventListFragment) getFragmentManager()
                            .findFragmentById(R.id.calendarevent_list);
                    calenderListFragment.getListView().setVisibility(View.VISIBLE);
                    calenderListFragment.setListAdapter(mListAdapter);

                    if (mDialog.isShowing())
                    {
                        mDialog.dismiss();
                        mParentActivity.setRequestedOrientation(mStoredRotation);
                    }
                    Toast.makeText(CalendarEventListActivity.this, "Events loaded",
                            Toast.LENGTH_LONG).show();
                    // load the data from the web
                    mApplication.setCalendarModel(mCalendarModel);
                }
                else
                {
                    if (mDialog.isShowing())
                    {
                        mDialog.dismiss();
                        mParentActivity.setRequestedOrientation(mStoredRotation);
                    }
                    Toast.makeText(CalendarEventListActivity.this, "No events to show",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Callback is called by model when a CRUD operation is finished.
    @Override
    public void onOperationComplete(final OperationResult opResult) {
        this.runOnUiThread(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                    CalendarEventListActivity.this.setRequestedOrientation(mStoredRotation);
                }

                Toast.makeText(CalendarEventListActivity.this, opResult.getOperationResult(),
                        Toast.LENGTH_LONG).show();
                CalendarEventListFragment calenderListFragment = (CalendarEventListFragment) getFragmentManager()
                        .findFragmentById(R.id.calendarevent_list);

                ((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                        .getListAdapter())
                        .notifyDataSetChanged();
                if (((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                        .getListAdapter()).isEmpty() == false)
                {
                    getSelectedItem(opResult.getId());

                    // Set the event id of the newly selected list event.
                    CalendarEventListActivity.this.selectedEventId = ((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                            .getListAdapter())
                            .getItem(0).id;

                }

                // Restore click event in the action buttons
                Button actionButton = (Button) CalendarEventListActivity.this
                        .findViewById(R.id.button_calendarUpdateEvent);
                actionButton.setClickable(true);
                actionButton = (Button) CalendarEventListActivity.this
                        .findViewById(R.id.button_calendarCreateEvent);
                actionButton.setClickable(true);
            }
        });
    }

    @Override
    public void onDialogPositiveClick(Fragment dialog, O365Calendar_Event newItem) {
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Adding an event...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(false);
        mDialog.setIndeterminate(true);
        mDialog.show();

        // Register callback with the model for notification of op complete
        mCalendarModel.setEventOperationCompleteListener(this);
        mCalendarModel.postCreatedEvent(this, newItem);
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