/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import java.util.Calendar;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.models.O365CalendarModel;

/**
 * A fragment representing a single CalendarEvent detail screen. This fragment is either contained
 * in a {@link CalendarEventListActivity} in two-pane mode (on tablets) or a
 * {@link CalendarEventDetailActivity} on handsets.
 */

public class CalendarEventDetailFragment extends Fragment
{

    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private O365CalendarModel.O365Calendar_Event mItem;

    private View rootView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public CalendarEventDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            // This event detail fragment view is owned by the event list activity in
            // large screen landscape mode. If in portrait mode for any screen size, the parent
            // activity is the CalenderEventDetailActivity.
            if (getActivity() instanceof CalendarEventDetailActivity)
            {
                CalendarEventDetailActivity bla = (CalendarEventDetailActivity) getActivity();
                mItem = bla.mCalendarModel
                        .getCalendar()
                        .ITEM_MAP
                                .get(getArguments()
                                        .getString(ARG_ITEM_ID));
            }
            else
            {
                CalendarEventListActivity calendarEventListActivity = (CalendarEventListActivity) getActivity();
                mItem = calendarEventListActivity.mCalendarModel
                        .getCalendar()
                        .ITEM_MAP
                                .get(getArguments()
                                        .getString(ARG_ITEM_ID));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.fragment_calendarevent_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            loadEventDetails();
        }

        return rootView;
    }

    private void loadEventDetails()
    {
        ((EditText) rootView.findViewById(R.id.locationText))
                .setText(mItem.getLocation());

        ((EditText) rootView.findViewById(R.id.subjectText))
                .setText(mItem.getSubject());

        ((EditText) rootView.findViewById(R.id.attendeesText))
                .setText(mItem.getAttendees());

        DatePicker startDatePicker = ((DatePicker) rootView.findViewById(R.id.StartDatePicker));
        startDatePicker.setEnabled(false);
        startDatePicker.init(
                mItem.getStartDateTime().get(Calendar.YEAR)
                , mItem.getStartDateTime().get(Calendar.MONTH)
                , mItem.getStartDateTime().get(Calendar.DAY_OF_MONTH), null);

        DatePicker endDatePicker = ((DatePicker) rootView.findViewById(R.id.EndDatePicker));
        endDatePicker.setEnabled(false);
        endDatePicker.init(
                mItem.getEndDateTime().get(Calendar.YEAR)
                , mItem.getEndDateTime().get(Calendar.MONTH)
                , mItem.getEndDateTime().get(Calendar.DAY_OF_MONTH), null);

        // Fill other calendar fields from calendar item

        TimePicker startClock = (
                (TimePicker) rootView
                        .findViewById(R.id.startTimePicker));

        startClock.setEnabled(false);
        startClock.setCurrentHour(
                mItem.getStartDateTime()
                        .get(Calendar.HOUR_OF_DAY));

        startClock.setCurrentMinute(
                mItem.getStartDateTime()
                        .get(Calendar.MINUTE));

        TimePicker endClock = ((TimePicker) rootView.findViewById(R.id.endTimePicker));
        endClock.setEnabled(false);
        endClock.setCurrentHour(
                mItem
                        .getEndDateTime()
                        .get(Calendar.HOUR_OF_DAY));

        endClock.setCurrentMinute(
                mItem.getEndDateTime()
                        .get(Calendar.MINUTE));

    }

    public void updateItem()
    {
        Editable subject = ((EditText) rootView.findViewById(R.id.subjectText))
                .getText();
        mItem.updateSubject(subject.toString());
        Editable location = ((EditText) rootView.findViewById(R.id.locationText))
                .getText();
        mItem.setLocation(location.toString());
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
