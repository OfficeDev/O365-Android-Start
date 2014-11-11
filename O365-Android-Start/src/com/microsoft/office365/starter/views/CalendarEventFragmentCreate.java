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
import android.widget.TimePicker;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.models.O365CalendarModel;

public class CalendarEventFragmentCreate extends Fragment implements View.OnClickListener
{
    O365CalendarModel.O365Calendar_Event mNewEventModel;
    CalendarEventListActivity mCalendarListActivity;
    NoticeDialogListener mListener;
    private View rootView;

    public CalendarEventFragmentCreate() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCalendarListActivity = (CalendarEventListActivity) getActivity();
        mNewEventModel = mCalendarListActivity.mCalendarModel.createEvent("New event");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.fragment_calendarevent_detail_create, container, false);

        rootView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(mNewEventModel);

                // Update event model with strings from create fragment
                saveEventDetails();
                getActivity().getFragmentManager().popBackStack();
                mListener.onDialogPositiveClick(CalendarEventFragmentCreate.this, mNewEventModel);
            }
        });
        rootView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // "Cancel". Reset to default values

                loadEventDetails();
                getActivity().getFragmentManager().popBackStack();
                mListener.onDialogNegativeClick(CalendarEventFragmentCreate.this);
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
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private void addItem(O365CalendarModel.O365Calendar_Event item) {
        mCalendarListActivity.mCalendarModel.getCalendar().ITEMS.add(item);
        mCalendarListActivity.mCalendarModel.getCalendar().ITEM_MAP.put(item.id, item);
    }

    private void loadEventDetails()
    {
        ((EditText) rootView.findViewById(R.id.locationText))
                .setText("My location");

        ((EditText) rootView.findViewById(R.id.subjectText))
                .setText("New event");

        ((EditText) rootView.findViewById(R.id.attendeesText))
                .setText("");

        DatePicker startDatePicker = ((DatePicker) rootView.findViewById(R.id.StartDatePicker));
        Calendar calendar = Calendar.getInstance();

        startDatePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONDAY),
                calendar.get(Calendar.DAY_OF_MONTH), null);

        DatePicker endDatePicker = ((DatePicker) rootView.findViewById(R.id.EndDatePicker));

        endDatePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONDAY),
                calendar.get(Calendar.DAY_OF_MONTH), null);

        TimePicker startClock = ((TimePicker) rootView.findViewById(R.id.startTimePicker));

        startClock.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        startClock.setCurrentMinute(calendar.get(Calendar.MINUTE));
        TimePicker endClock = ((TimePicker) rootView.findViewById(R.id.endTimePicker));
        endClock.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        endClock.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    // Saves the user's choices in the event model before posting new event to Outlook service
    private void saveEventDetails()
    {
        Editable subject = ((EditText) rootView.findViewById(R.id.subjectText))
                .getText();
        mNewEventModel.updateSubject(subject.toString());

        Editable location = ((EditText) rootView.findViewById(R.id.locationText))
                .getText();
        mNewEventModel.setLocation(location.toString());

        Editable attendee = ((EditText) rootView.findViewById(R.id.attendeesText))
                .getText();
        mNewEventModel.setAttendees(attendee.toString());

        DatePicker startDatePicker = ((DatePicker) rootView.findViewById(R.id.StartDatePicker));
        TimePicker startClock = ((TimePicker) rootView.findViewById(R.id.startTimePicker));
        mNewEventModel.setStartDate(
                startDatePicker.getYear(),
                startDatePicker.getMonth(),
                startDatePicker.getDayOfMonth(),
                startClock.getCurrentHour(),
                startClock.getCurrentMinute());

        DatePicker endDatePicker = ((DatePicker) rootView.findViewById(R.id.EndDatePicker));
        TimePicker endClock = ((TimePicker) rootView.findViewById(R.id.endTimePicker));
        mNewEventModel.setEndDate(
                endDatePicker.getYear(),
                endDatePicker.getMonth(),
                endDatePicker.getDayOfMonth(),
                endClock.getCurrentHour(),
                endClock.getCurrentMinute());
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