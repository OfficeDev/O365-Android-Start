/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import java.util.Calendar;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.models.O365CalendarModel;

public class CalendarEventFragmentUpdate extends Fragment implements View.OnClickListener

{
    private O365CalendarModel.O365Calendar_Event mO365Event;
    private CalendarEventListActivity mCalenderListActivity;
    NoticeDialogListener mListener;

    private View rootView;
    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public CalendarEventFragmentUpdate() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mCalenderListActivity = (CalendarEventListActivity) getActivity();
            mO365Event = mCalenderListActivity.mCalendarModel
                    .getCalendar()
                    .ITEM_MAP
                            .get(getArguments()
                                    .getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.fragment_calendarevent_detail_create
                , container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.CalendarDetailFragmentTitle);
        titleView.setText(R.string.Calendar_UpdateEventDetails);
        // Load the properties of the event to update into UI
        loadEventDetails();

        // Done button click event handler
        rootView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Done button"
                saveEventDetails();

                // Close the update fragment and return to the previous view fragment
                getActivity()
                        .getFragmentManager()
                        .popBackStack();
                
                // Call the listener's callback method to inform that user is done
                mListener.onDialogPositiveClick(CalendarEventFragmentUpdate.this);


            }
        });

        // Cancel button click event handler
        rootView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .getFragmentManager()
                        .popBackStack();
                // "Cancel". Reset to default values
                mListener.onDialogNegativeClick(CalendarEventFragmentUpdate.this);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            // Register the callback method of a listening object
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    // Save event property changes before posting to Outlook service
    private void saveEventDetails()
    {
        Editable subject = ((EditText) rootView.findViewById(R.id.subjectText))
                .getText();
        mO365Event.updateSubject(subject.toString());

        Editable location = ((EditText) rootView.findViewById(R.id.locationText))
                .getText();
        mO365Event.setLocation(location.toString());

        Editable attendee = ((EditText) rootView.findViewById(R.id.attendeesText))
                .getText();
        mO365Event.setAttendees(attendee.toString());

        DatePicker startDatePicker = ((DatePicker) rootView.findViewById(R.id.StartDatePicker));
        TimePicker startClock = ((TimePicker) rootView.findViewById(R.id.startTimePicker));
        mO365Event.setStartDate(
                startDatePicker.getYear(),
                startDatePicker.getMonth(),
                startDatePicker.getDayOfMonth(),
                startClock.getCurrentHour(),
                startClock.getCurrentMinute());

        DatePicker endDatePicker = ((DatePicker) rootView.findViewById(R.id.EndDatePicker));
        TimePicker endClock = ((TimePicker) rootView.findViewById(R.id.endTimePicker));
        mO365Event.setEndDate(
                endDatePicker.getYear(),
                endDatePicker.getMonth(),
                endDatePicker.getDayOfMonth(),
                endClock.getCurrentHour(),
                endClock.getCurrentMinute());

    }

    // Load the properties of the event to update
    private void loadEventDetails()
    {
        ((EditText) rootView.findViewById(R.id.locationText))
                .setText(mO365Event.getLocation());

        ((EditText) rootView.findViewById(R.id.subjectText))
                .setText(mO365Event.getSubject());

        ((EditText) rootView.findViewById(R.id.attendeesText))
                .setText(mO365Event.getAttendees());

        DatePicker startDatePicker = ((DatePicker) rootView.findViewById(R.id.StartDatePicker));
        startDatePicker.init(mO365Event.getStartDateTime()
                .get(Calendar.YEAR), mO365Event.getStartDateTime()
                .get(Calendar.MONTH), mO365Event.getStartDateTime()
                .get(Calendar.DAY_OF_MONTH), null);

        DatePicker endDatePicker = ((DatePicker) rootView.findViewById(R.id.EndDatePicker));
        endDatePicker.init(mO365Event.getEndDateTime()
                .get(Calendar.YEAR), mO365Event.getEndDateTime()
                .get(Calendar.MONTH), mO365Event.getEndDateTime()
                .get(Calendar.DAY_OF_MONTH), null);

        // Fill other calendar fields from calendar item

        TimePicker startClock = ((TimePicker) rootView.findViewById(R.id.startTimePicker));
        startClock.setCurrentHour(mO365Event.getStartDateTime()
                .get(Calendar.HOUR_OF_DAY));

        startClock.setCurrentMinute(mO365Event.getStartDateTime()
                .get(Calendar.MINUTE));

        TimePicker endClock = ((TimePicker) rootView.findViewById(R.id.endTimePicker));
        endClock.setCurrentHour(mO365Event.getEndDateTime()
                .get(Calendar.HOUR_OF_DAY));
        endClock.setCurrentMinute(mO365Event.getEndDateTime()
                .get(Calendar.MINUTE));

    }

    @Override
    public void onClick(View arg0) {

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