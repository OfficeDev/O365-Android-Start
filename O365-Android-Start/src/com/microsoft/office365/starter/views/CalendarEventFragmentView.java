/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.NoticeDialogListener;
import com.microsoft.office365.starter.models.O365CalendarModel;

public class CalendarEventFragmentView extends Fragment implements View.OnClickListener,
        OnItemSelectedListener
{
    private O365CalendarModel.O365Calendar_Event mEventModel;
    private O365CalendarModel mCalendarModel;
    private NoticeDialogListener mListener;
    private View rootView;
    public static final String ARG_ITEM_ID = "item_id";
    private boolean mCreateMode = true;

    public CalendarEventFragmentView() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String parentActivityName = getActivity().getClass().getName();
        if (parentActivityName.contains("CalendarEventDetailActivity"))
        {
            mCalendarModel = ((CalendarEventDetailActivity) getActivity()).mCalendarModel;
        }
        else
        {
            mCalendarModel = ((CalendarEventListActivity) getActivity()).mCalendarModel;
        }

        // If this activity is opened to edit an existing event, the item id is passed in the intent
        // bundle.
        // Otherwise, the activity is opened to create a new event and no arguments are passed
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID))
        {
            String itemId = getArguments()
                    .getString(ARG_ITEM_ID);
            if (itemId != null)
            {
                mEventModel = mCalendarModel
                        .getCalendar()
                        .ITEM_MAP
                                .get(itemId);

                // Set event create mode flag to false.
                mCreateMode = false;
            }
        }
        else
        {
            mEventModel = mCalendarModel.createEvent("New event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.fragment_calendarevent_detail_create, container, false);

        if (mCreateMode == false)
        {
            TextView titleView = (TextView) rootView.findViewById(R.id.CalendarDetailFragmentTitle);
            titleView.setText(R.string.Calendar_UpdateEventDetails);
            loadEventDetails();
        }
        else
        {
            loadSpinners();
        }

        rootView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update event model with strings from create fragment
                saveEventDetails();
                if (mCreateMode == false)
                {
                    mListener.onDialogPositiveClick(CalendarEventFragmentView.this,
                            mEventModel, false);
                }
                else
                {
                    addItem(mEventModel);
                    mListener.onDialogPositiveClick(CalendarEventFragmentView.this,
                            mEventModel, true);
                }
            }
        });
        rootView.findViewById(R.id.actionbar_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mListener.onDialogNegativeClick(CalendarEventFragmentView.this);
            }
        });
        return rootView;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        Activity parent = getActivity();

        // If the parent activity is the tablet (two pane) activity, then action buttons
        // have been disabled. The buttons must be enabled again.
        // If the parent activity is the small screen activity, then there are no action
        // buttons to enable.
        if (parent.getClass().getName().equals("CalendarEventListActivity"))
        {
            CalendarEventListActivity parentList = (CalendarEventListActivity) getActivity();
            if (parentList != null)
                parentList.helperEnableActionButtons();
        }
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
        mCalendarModel.getCalendar().ITEMS.add(item);
        mCalendarModel.getCalendar().ITEM_MAP.put(item.getID(), item);
    }

    private void loadEventDetails()
    {
        if (mCreateMode == true)
        {
            ((EditText) rootView.findViewById(R.id.locationText))
                    .setText("My location");

            ((EditText) rootView.findViewById(R.id.subjectText))
                    .setText("New event");

            ((EditText) rootView.findViewById(R.id.attendeesText))
                    .setText("");
        }
        else
        {
            ((EditText) rootView.findViewById(R.id.locationText))
                    .setText(mEventModel.getLocation());

            ((EditText) rootView.findViewById(R.id.subjectText))
                    .setText(mEventModel.getSubject());

            ((EditText) rootView.findViewById(R.id.attendeesText))
                    .setText(mEventModel.getAttendees());

            // Fill the event start and end date/time spinners on UI
            loadSpinners();
        }
    }

    private void loadSpinners()
    {

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , R.array.month_abbr
                , android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , R.array.Hour_Spinner
                , android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> minuteAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , R.array.Minute_Spinner
                , android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> meridianAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , R.array.Meridian_Spinner
                , android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , R.array.DOM_31
                , android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> yearAdapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);
        yearAdapter.add("2013");
        yearAdapter.add("2014");
        yearAdapter.add("2015");

        Spinner startMonthSpinner = (Spinner) rootView.findViewById(R.id.StartMonth_Spinner);
        startMonthSpinner.setAdapter(monthAdapter);
        startMonthSpinner.setOnItemSelectedListener((OnItemSelectedListener) this);

        Spinner startDaySpinner = (Spinner) rootView.findViewById(R.id.StartDay_Spinner);
        startDaySpinner.setAdapter(dayAdapter);
        startDaySpinner.setOnItemSelectedListener(this);

        Spinner endMonthSpinner = (Spinner) rootView.findViewById(R.id.endMonth_Spinner);
        endMonthSpinner.setAdapter(monthAdapter);
        endMonthSpinner.setOnItemSelectedListener((OnItemSelectedListener) this);

        Spinner startYearSpinner = (Spinner) rootView.findViewById(R.id.StartYear_Spinner);
        startYearSpinner.setAdapter(yearAdapter);
        startYearSpinner.setOnItemSelectedListener(this);

        Spinner endYearSpinner = (Spinner) rootView.findViewById(R.id.endYear_Spinner);
        endYearSpinner.setAdapter(yearAdapter);

        Spinner endDaySpinner = (Spinner) rootView.findViewById(R.id.endDay_Spinner);
        endDaySpinner.setAdapter(dayAdapter);

        Spinner startHourSpinner = (Spinner) rootView.findViewById(R.id.Hour_Spinner);
        startHourSpinner.setAdapter(hourAdapter);
        startHourSpinner.setOnItemSelectedListener(this);

        Spinner startMinuteSpinner = (Spinner) rootView.findViewById(R.id.Minute_Spinner);
        startMinuteSpinner.setAdapter(minuteAdapter);
        startMinuteSpinner.setOnItemSelectedListener(this);

        Spinner startMeridanSpinner = (Spinner) rootView.findViewById(R.id.Meridan_Spinner);
        startMeridanSpinner.setAdapter(meridianAdapter);
        startMeridanSpinner.setOnItemSelectedListener(this);

        Spinner endMeridianSpinner = (Spinner) rootView.findViewById(R.id.EndMeridan_Spinner);
        endMeridianSpinner.setAdapter(meridianAdapter);

        Spinner endMinuteSpinner = (Spinner) rootView.findViewById(R.id.EndMinute_Spinner);
        endMinuteSpinner.setAdapter(minuteAdapter);

        Spinner endHourSpinner = (Spinner) rootView.findViewById(R.id.EndHour_Spinner);
        endHourSpinner.setAdapter(hourAdapter);

        // If in EDIT mode, fill the date/time spinner values from the existing calendar event
        if (!mCreateMode)
        {
            Calendar startCalendar = mEventModel.getStartDateTime();

            String aString = Integer.toString(startCalendar.get(Calendar.YEAR));
            int position = yearAdapter.getPosition(aString);
            startYearSpinner.setSelection(position);

            startMonthSpinner.setSelection(startCalendar.get(Calendar.MONTH));

            Calendar endCalendar = mEventModel.getEndDateTime();
            aString = Integer.toString(endCalendar.get(Calendar.YEAR));
            position = yearAdapter.getPosition(aString);
            endYearSpinner.setSelection(position);
            endMonthSpinner.setSelection(endCalendar.get(Calendar.MONTH));

            int startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
            if (startHour > 12)
            {
                startHour -= 12;
                startMeridanSpinner.setSelection(1);
            }
            else
            {
                startMeridanSpinner.setSelection(0);
            }

            aString = Integer.toString(startHour);
            position = hourAdapter.getPosition(aString);
            startHourSpinner.setSelection(position);

            aString = Integer.toString(startCalendar.get(Calendar.MINUTE));
            position = minuteAdapter.getPosition(aString);
            startMinuteSpinner.setSelection(position);

            int endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
            if (endHour > 12)
            {
                endHour -= 12;
                endMeridianSpinner.setSelection(1);
            }
            else
            {
                endMeridianSpinner.setSelection(0);
            }
            aString = Integer.toString(endHour);
            position = hourAdapter.getPosition(aString);
            endHourSpinner.setSelection(position);

            aString = Integer.toString(endCalendar.get(Calendar.MINUTE));
            position = minuteAdapter.getPosition(aString);
            endMinuteSpinner.setSelection(position);
        }
    }

    // Saves the user's choices in the event model before posting new event to Outlook service
    private void saveEventDetails()
    {
        Pattern pattern;
        Matcher matcher;

        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(EMAIL_PATTERN);
        Editable subject = ((EditText) rootView.findViewById(R.id.subjectText))
                .getText();
        mEventModel.updateSubject(subject.toString());

        Editable location = ((EditText) rootView.findViewById(R.id.locationText))
                .getText();
        mEventModel.setLocation(location.toString());

        Editable attendee = ((EditText) rootView.findViewById(R.id.attendeesText))
                .getText();

        // The comma delimited list of attendees from UI
        String[] attendeeArray = attendee.toString().split(";");
        // Iterate on attendee array
        StringBuilder sBuilder = new StringBuilder();
        for (String attendeeString : attendeeArray)
        {
            // Validate the attendee string as an email
            matcher = pattern.matcher(attendeeString.trim());
            if (matcher.matches())
                sBuilder.append(attendeeString.trim() + ";");
        }
        mEventModel.setAttendees(sBuilder.toString());

        Spinner startMonthSpin = (Spinner) rootView.findViewById(R.id.StartMonth_Spinner);
        Spinner startYearSpin = (Spinner) rootView.findViewById(R.id.StartYear_Spinner);
        Spinner startDaySpin = (Spinner) rootView.findViewById(R.id.StartDay_Spinner);
        Spinner startHourSpin = (Spinner) rootView.findViewById(R.id.Hour_Spinner);
        Spinner startMinSpin = (Spinner) rootView.findViewById(R.id.Minute_Spinner);
        Spinner startMerSpin = (Spinner) rootView.findViewById(R.id.Meridan_Spinner);
        int startHour = Integer.parseInt(startHourSpin.getSelectedItem().toString());
        if (startMerSpin.getSelectedItemPosition() == 1)
            startHour += 12;

        mEventModel.setStartDate(
                Integer.parseInt(startYearSpin.getSelectedItem().toString()),
                startMonthSpin.getSelectedItemPosition(),
                Integer.parseInt(startDaySpin.getSelectedItem().toString()),
                startHour,
                Integer.parseInt(startMinSpin.getSelectedItem().toString()));

        Spinner endMonthSpin = (Spinner) rootView.findViewById(R.id.endMonth_Spinner);
        Spinner endYearSpin = (Spinner) rootView.findViewById(R.id.endYear_Spinner);
        Spinner endDaySpin = (Spinner) rootView.findViewById(R.id.endDay_Spinner);
        Spinner endHourSpin = (Spinner) rootView.findViewById(R.id.EndHour_Spinner);
        Spinner endMinSpin = (Spinner) rootView.findViewById(R.id.EndMinute_Spinner);
        Spinner endMerSpin = (Spinner) rootView.findViewById(R.id.EndMeridan_Spinner);
        int endHour = Integer.parseInt(endHourSpin.getSelectedItem().toString());
        if (endMerSpin.getSelectedItemPosition() == 1)
            endHour += 12;

        mEventModel.setEndDate(
                Integer.parseInt(endYearSpin.getSelectedItem().toString()),
                endMonthSpin.getSelectedItemPosition(),
                Integer.parseInt(endDaySpin.getSelectedItem().toString()),
                endHour,
                Integer.parseInt(endMinSpin.getSelectedItem().toString()));
    }

    @Override
    public void onClick(View arg0) {
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        // If in event edit mode, do not default event end date/time when start date/time is changed
        if (!mCreateMode)
            return;

        // Create mode, default end date/time values to chosen start date/time values
        switch (arg0.getId())
        {
            case R.id.StartMonth_Spinner:
                processSelectedStartMonth(arg2);
                break;
            case R.id.endMonth_Spinner:
                processEndMonthSpinner(arg2);
                break;
            case R.id.StartDay_Spinner:
                Spinner endDaySpinner = (Spinner) rootView.findViewById(R.id.endDay_Spinner);
                endDaySpinner.setSelection(arg2);
                break;
            case R.id.StartYear_Spinner:
                Spinner endYearSpinner = (Spinner) rootView.findViewById(R.id.endYear_Spinner);
                endYearSpinner.setSelection(arg2);
                break;
            case R.id.Hour_Spinner:
                Spinner endHourSpinner = (Spinner) rootView.findViewById(R.id.EndHour_Spinner);
                endHourSpinner.setSelection(arg2);
                break;
            case R.id.Minute_Spinner:
                break;
            case R.id.Meridan_Spinner:
                Spinner endMeridianSpinner = (Spinner) rootView
                        .findViewById(R.id.EndMeridan_Spinner);
                endMeridianSpinner.setSelection(arg2);
                break;
        }
    }

    // Set the day of month adapter to appropriate ArrayAdaptor for chosen month
    private void processSelectedStartMonth(int arg2)
    {
        int resource = 0;
        switch (arg2)
        {
            case 0: // Jan.
                resource = R.array.DOM_31;
                break;
            case 1: // Feb.
                resource = R.array.DOM_29;
                break;
            case 2: // Mar.
                resource = R.array.DOM_31;
                break;
            case 3: // Apr.
                resource = R.array.DOM_30;
                break;
            case 4: // May.
                resource = R.array.DOM_31;
                break;
            case 5: // Jun.
                resource = R.array.DOM_30;
                break;
            case 6: // Jul.
                resource = R.array.DOM_31;
                break;
            case 7: // Aug.
                resource = R.array.DOM_31;
                break;
            case 8: // Sep.
                resource = R.array.DOM_30;
                break;
            case 9: // Oct.
                resource = R.array.DOM_31;
                break;
            case 10: // Nov.
                resource = R.array.DOM_30;
                break;
            case 11: // Dec.
                resource = R.array.DOM_31;

        }

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , resource
                , android.R.layout.simple_spinner_dropdown_item);

        // Set day of month spinner to ArrayAdapter for month chosen
        Spinner startDaySpinner = (Spinner) rootView.findViewById(R.id.StartDay_Spinner);
        startDaySpinner.setAdapter(dayAdapter);

        // Default end month spinner to start month selected
        Spinner endMonthSpinner = (Spinner) rootView.findViewById(R.id.endMonth_Spinner);
        endMonthSpinner.setSelection(arg2);
    }

    private void processEndMonthSpinner(int arg2)
    {
        int resource = 0;
        switch (arg2)
        {
            case 0:
                resource = R.array.DOM_31;
                break;
            case 1:
                resource = R.array.DOM_29;
                break;
            case 2:
                resource = R.array.DOM_31;
                break;
            case 3:
                resource = R.array.DOM_30;
                break;
            case 4:
                resource = R.array.DOM_31;
                break;
            case 5:
                resource = R.array.DOM_30;
                break;
            case 6:
                resource = R.array.DOM_31;
                break;
            case 7:
                resource = R.array.DOM_31;
                break;
            case 8:
                resource = R.array.DOM_30;
                break;
            case 9:
                resource = R.array.DOM_31;
                break;
            case 10:
                resource = R.array.DOM_30;
                break;
            case 11:
                resource = R.array.DOM_31;

        }
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                getActivity()
                , resource
                , android.R.layout.simple_spinner_dropdown_item);
        Spinner endDaySpinner = (Spinner) rootView.findViewById(R.id.endDay_Spinner);
        endDaySpinner.setAdapter(dayAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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
