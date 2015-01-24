/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Calendar;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.interfaces.OnEventsAddedListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.Calendar.O365CalendarModel.O365Calendar_Event;
import com.microsoft.office365.starter.helpers.DeleteDialogFragment;
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

import static com.microsoft.office365.starter.Calendar.O365CalendarModel.*;

/**
 * An activity representing a list of CalendarEvents. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link com.microsoft.office365.starter.Calendar.CalendarEventDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link CalendarEventListFragment} and the item details (if present) is a
 * {@link com.microsoft.office365.starter.Calendar.CalendarEventDetailFragment}.
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

    private String selectedEventId = "";
    public O365CalendarModel.CalendarEvents calendarEvents;
    public O365CalendarModel mCalendarModel;
    private android.app.FragmentManager mFragmentManager;
    private CalendarEventListActivity mParentActivity;
    private DeleteDialogFragment mDeleteFragment;
    private int mEventPageNumber;
    private int mEventPageSize = 11;

    /** The m stored rotation. */
    private ProgressDialog mDialog;
    private O365APIsStart_Application mApplication;

    private ArrayAdapter<O365CalendarModel.O365Calendar_Event> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (O365APIsStart_Application) this.getApplication();
        mCalendarModel = mApplication.getCalendarModel();
        mParentActivity = this;

        this.setTitle(R.string.mainButton_Calendar);
        if (mCalendarModel == null)
            mCalendarModel = new O365CalendarModel(this);

        mCalendarModel.setActivity(this);

        mApplication.setCalendarModel(mCalendarModel);

        //Get the calender model items collection class object
        calendarEvents = mCalendarModel.getCalendar();

        // Load the calendar list activity
        setContentView(R.layout.activity_calendarevent_list);
        if (findViewById(R.id.calendarevent_detail_container) != null)
            mTwoPane = true;

        CalendarEventListFragment eventListFragment = (CalendarEventListFragment) getFragmentManager()
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

       // calenderListFragment.getListView().SETSCR
        if ((mApplication.getCalendarModel() != null) && !mApplication
                .getCalendarModel()
                .getCalendar()
                .ITEMS.isEmpty())
        {
            CalendarEventListFragment calendarEventListFragment = (CalendarEventListFragment) getFragmentManager()
                    .findFragmentById(R.id.calendarevent_list);
            mListAdapter = new ArrayAdapter<O365CalendarModel.O365Calendar_Event>(this,
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1, mApplication.getCalendarModel().getCalendar().ITEMS);
            calendarEventListFragment.setListAdapter(mListAdapter);
        }
        else
            helperGetEventList();

        mFragmentManager = getFragmentManager();

        if (mTwoPane)
        {
            // Load the overview text into the WebView
            WebView introView = (WebView) findViewById(R.id.CalendarStarterTextWebView);
            introView.setBackgroundColor(getResources().getColor(
                    R.color.ApplicationPageBackgroundThemeBrush));
            String introHTML = getResources().getString(R.string.calendar_view_intro);
            introView.loadData(introHTML, "text/html", "UTF-8");
            introView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Reload the event list when the activity resumes
        // only if the list is not currently being loaded from the onCreated callback
        if (mDialog != null && !mDialog.isShowing())
            helperGetEventList();

        if (mTwoPane)
            helperEnableActionButtons();
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

        if (mTwoPane)
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            actionGetSelectedItem();

    }

    private void actionGetSelectedItem()
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
    public void onDeleteButtonClick(View view)
    {
        actionRemoveEvent(view);
    }

    // Fill the list with calendar events
    public void onClickGetEventsButton(View view)
    {
        helperGetEventList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.event_create:
                actionCreateNewEvent();
                break;
            case R.id.event_edit:
                actionEditEvent(null);
                break;
            case R.id.event_remove:
                actionRemoveEvent(null);
                break;
            case R.id.event_refresh:
                helperGetEventList();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickEditButton(View view)
    {
        if (this.selectedEventId.length() == 0)
            return;

        actionEditEvent(view);
    }

    private void actionRemoveEvent(View view)
    {
        if (this.selectedEventId.length() == 0)
        {
            Toast.makeText(CalendarEventListActivity.this, "Select an event to delete",
                    Toast.LENGTH_LONG).show();
            return;
        }

        O365Calendar_Event event = calendarEvents.ITEM_MAP.get(this.selectedEventId);
        if (event == null)
        {
            Toast.makeText(CalendarEventListActivity.this, "Null event selected",
                    Toast.LENGTH_LONG).show();
            return;

        }
        Bundle arguments = new Bundle();
        arguments.putString("MessageString", "Delete " + event.getSubject() + "?");
        mDeleteFragment = new DeleteDialogFragment();
        mDeleteFragment.setArguments(arguments);
        mDeleteFragment.show(mFragmentManager, "Delete this event?");

    }

    private void actionEditEvent(View view)
    {
        if (this.selectedEventId.length() == 0)
        {
            Toast.makeText(CalendarEventListActivity.this, "Select an event to update",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (mTwoPane == true)
        {
            helperDisableActionMenuItems();
            helperDisableActionButtons();

            Bundle arguments = new Bundle();
            arguments.putString(CalendarEventDetailFragment.ARG_ITEM_ID, this.selectedEventId);
            CalendarEventFragmentView updateFragment = new CalendarEventFragmentView();
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
            Bundle bundle = new Bundle();
            bundle.putString(CalendarEventDetailFragment.ARG_ITEM_ID, this.selectedEventId);
            detailIntent.putExtras(bundle);
            detailIntent.setAction(Intent.ACTION_EDIT);
            startActivity(detailIntent);
        }
        // Need code that reacts to popping update fragment of of stack. The
        // required code would reload the view to show the updated fragment details.

    }

    // Create new calendar event
    public void onClickNewEventButton(View view)
    {
        actionCreateNewEvent();
    }

    // Opens create event fragment and swaps with current event detail fragment
    private void actionCreateNewEvent()
    {
        if (mTwoPane)
        {

            helperDisableActionButtons();
            helperDisableActionMenuItems();

            CalendarEventFragmentView createFragment = new CalendarEventFragmentView();
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
            Bundle bundle = new Bundle();
            detailIntent.putExtras(bundle);
            detailIntent.setAction(Intent.ACTION_INSERT);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onBackStackChanged()
    {
        if (mTwoPane)
            helperEnableActionButtons();

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
        getMenuInflater().inflate(R.menu.calendar_menu, menu);
        return true;
    }

    // Callback called by update and delete fragments when user clicks the
    // Done button on the fragments. This callback method is used in large screen
    // device mode only.
    // For small screens, events are posted to the Exchange service from the
    // CalendarEventDetailActivity class.
    @Override
    public void onDialogPositiveClick(Fragment dialog) {

        if (dialog == mDeleteFragment)
        {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            mParentActivity = this;
            mDialog = new ProgressDialog(this);
            mDialog.setTitle("Removing an event...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(true);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mCalendarModel.setEventOperationCompleteListener(this);
            /*
      The dummy content this fragment is presenting.
     */
            O365Calendar_Event mItem = mCalendarModel.getCalendar().ITEM_MAP.get(this.selectedEventId);
            mCalendarModel.postDeletedEvent(this, mItem);
        }
    }

    // Callback is called when user clicks the Cancel button on the update or delete event fragments
    @Override
    public void onDialogNegativeClick(Fragment dialog)
    {

        // In small screen layout, the update actionButton is not loaded
        if (mTwoPane)
        {
            this.getFragmentManager()
                    .popBackStack();
            actionGetSelectedItem();
            helperEnableActionButtons();
        }
        helperEnableActionMenuItems();
    }

    public void helperEnableActionMenuItems()
    {
        View eventMenuItem = mParentActivity.findViewById(R.id.event_create);
        eventMenuItem.setClickable(true);
        eventMenuItem.setEnabled(true);
        eventMenuItem = mParentActivity.findViewById(R.id.event_remove);
        eventMenuItem.setClickable(true);
        eventMenuItem.setEnabled(true);
        eventMenuItem = mParentActivity.findViewById(R.id.event_edit);
        eventMenuItem.setClickable(true);
        eventMenuItem.setEnabled(true);
        eventMenuItem = mParentActivity.findViewById(R.id.event_refresh);
        eventMenuItem.setClickable(true);
        eventMenuItem.setEnabled(true);

    }

    public void helperEnableActionButtons()
    {
        // Restore click event in the action buttons
        Button actionButton = (Button) CalendarEventListActivity.this
                .findViewById(R.id.button_calendarUpdateEvent);
        actionButton.setClickable(true);

        actionButton = (Button) CalendarEventListActivity.this
                .findViewById(R.id.button_calendarCreateEvent);
        actionButton.setClickable(true);

        // Enable delete button
        actionButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarDeleteEvent);
        actionButton.setClickable(true);

        Button refreshButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarGetEvents);
        refreshButton.setClickable(true);
    }

    public void helperDisableActionMenuItems()
    {
        View eventMenuItem = mParentActivity.findViewById(R.id.event_create);
        eventMenuItem.setClickable(false);
        eventMenuItem.setEnabled(false);
        eventMenuItem = mParentActivity.findViewById(R.id.event_remove);
        eventMenuItem.setClickable(false);
        eventMenuItem.setEnabled(false);
        eventMenuItem = mParentActivity.findViewById(R.id.event_edit);
        eventMenuItem.setClickable(false);
        eventMenuItem.setEnabled(false);
        eventMenuItem = mParentActivity.findViewById(R.id.event_refresh);
        eventMenuItem.setClickable(false);
        eventMenuItem.setEnabled(false);

    }

    public void helperDisableActionButtons()
    {
        // Disable action buttons until create event action is complete
        Button actionButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarUpdateEvent);
        actionButton.setClickable(false);

        actionButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarCreateEvent);
        actionButton.setClickable(false);

        // Disable action buttons until create event action is complete
        Button deleteButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarDeleteEvent);
        deleteButton.setClickable(false);

        Button refreshButton = (Button) mParentActivity
                .findViewById(R.id.button_calendarGetEvents);
        refreshButton.setClickable(false);
    }

    // Called when the user click the Get Events button on this activity
    public void helperGetEventList()
    {
        mDialog = new ProgressDialog(mParentActivity);
        mDialog.setTitle("Retrieving Events...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(true);
        mDialog.setIndeterminate(true);
        mDialog.show();

        // Register a callback on the event model to be called
        // when events are retrieved from Outlook service
        mCalendarModel.setEventAddedListener(this);
        
        //Set the event page size to 11, and start paging at first event
        mCalendarModel.getEventList(mEventPageSize, mEventPageNumber);

        //TODO move this code to a "next page of events" button
       // mEventPageNumber += mEventPageSize;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save user's current state
        mApplication.setCalendarModel(mCalendarModel);
    }

    @Override
    public void OnEventsAdded(final setEventCollection eventCollection) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                CalendarEventListFragment calendarListFragment = (CalendarEventListFragment) getFragmentManager()
                        .findFragmentById(R.id.calendarevent_list);
                if (!eventCollection.getEventCollection().isEmpty())
                {
                    // Not necessary to check the ArrayAdapter type because the type is always set
                    // as cast in the
                    // following code
                    ((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calendarListFragment
                            .getListAdapter())
                            .notifyDataSetChanged();

                    calendarListFragment.getListView().setVisibility(View.VISIBLE);
                    calendarListFragment.setListAdapter(mListAdapter);

                    if (mDialog.isShowing())
                        mDialog.dismiss();

                    Toast.makeText(CalendarEventListActivity.this, "Events loaded",
                            Toast.LENGTH_LONG).show();
                    // load the data from the web
                    mApplication.setCalendarModel(mCalendarModel);

                }
                else
                {
                    if (mDialog.isShowing())
                        mDialog.dismiss();

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

                
                //Close progress dialog and unlock device orientation changes
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                if (mDialog.isShowing())
                    mDialog.dismiss();


                //Inform user via toast about the results of the operation
                Toast.makeText(CalendarEventListActivity.this, opResult.getOperationResult(),
                        Toast.LENGTH_LONG).show();
                CalendarEventListFragment calenderListFragment = (CalendarEventListFragment) getFragmentManager()
                        .findFragmentById(R.id.calendarevent_list);

                //Notify the list adaptor that the underlying event list has changed
                ((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                        .getListAdapter())
                        .notifyDataSetChanged();

                //Update the detail fragment with the results of the operation
                if (!opResult.getOperation().contains("Remove event"))
                {
                    //Get the event that was added or changed and then load its details in the right pane
                    //of the activity
                    if (((ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                            .getListAdapter()).isEmpty() == false)
                    {
                        String resultId = opResult.getId();
                        
                        //result of -1 indicates an operation failure
                        if (!resultId.contains("-1"))
                        {
                            CalendarEventListActivity.this.selectedEventId = resultId;
                            actionGetSelectedItem();
                        }
                    }
                }
                else
                {
                    //If remove event operation, select first event in the list and show its details
                    //or clear the right pane of the activity if there are no events in the list
                    CalendarEventListActivity.this.selectedEventId = "";
                    // In small screen layout, the detail fragments are not loaded into the list
                    // activity
                    if (mTwoPane)
                    {
                        // Close the update fragment, get the first item in the event list, and display details
                        mFragmentManager.popBackStack();
                        
                        ArrayAdapter<O365CalendarModel.O365Calendar_Event> eventList = (ArrayAdapter<O365CalendarModel.O365Calendar_Event>) calenderListFragment
                        .getListAdapter();
                        if (eventList.getCount() > 0)
                            CalendarEventListActivity.this.selectedEventId =  eventList.getItem(0).getID();
                        
                        //If list is empty, selected event Id will have length of zero
                        if (selectedEventId.length() > 0)
                            actionGetSelectedItem();
                        else
                            mFragmentManager.beginTransaction()
                            .remove(detailFragment)
                            .commit();
                    }
                }

                // In small screen layout, the update actionButton is not loaded
                if (mTwoPane)
                    helperEnableActionButtons();

                helperEnableActionMenuItems();

                // figure out if user added an event and call this method if they did
                if (opResult.getOperation().equals("Add event"))
                    helperGetEventList();
            }
        });
    }

    @Override
    public void onDialogPositiveClick(Fragment dialog, O365Calendar_Event editedEvent,
            boolean newItemFlag) {
        this.getFragmentManager()
                .popBackStack();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

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

        // Close the update fragment and return to the previous view fragment
        this.getFragmentManager()
                .popBackStack();
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
