/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.outlookservices.Attendee;
import com.microsoft.outlookservices.BodyType;
import com.microsoft.outlookservices.EmailAddress;
import com.microsoft.outlookservices.Event;
import com.microsoft.outlookservices.ItemBody;
import com.microsoft.outlookservices.Location;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.interfaces.OnEventsAddedListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.interfaces.OnEventsAddedListener.setEventCollection;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener.OperationResult;

/**
* This model class encapsulates all of the Outlook service API calendar operations that create
* read, update, and delete calendar events. The O365CalendarModel class contains several methods
* that post changes made to the calendar event (com.microsoft.office365.OutlookServices.Event) in
* the local cache. The CalendarEvents class exposes a list and hash table to be consumed by an
* arrayAdapter on the UI calendar event list The O365Calendar_Event class encapsulates
* com.microsoft.office365.OutlookServices.Event and exposes event properties as simple strings that
* the UI fragments consume.
*/
public class O365CalendarModel {

    CalendarEvents mCalendarEvents;
    O365APIsStart_Application mApplication;
    private OnEventsAddedListener mEventSelectionListener;
    private OnOperationCompleteListener mEventOperationCompleteListener;

    public void setEventSelectionListener(OnEventsAddedListener eventSelectionListener) {
        this.mEventSelectionListener = eventSelectionListener;
    }

    public void setEventOperationCompleteListener(
            OnOperationCompleteListener eventOperationCompleteListener)
    {
        this.mEventOperationCompleteListener = eventOperationCompleteListener;
    }

    // Returns an instance of the CalendarEvents class after constructing it if necessary
    public CalendarEvents getCalendar()
    {
        if (mCalendarEvents == null)
            mCalendarEvents = new CalendarEvents();
        return mCalendarEvents;
    }

    // This overload is called when a user is creating a new event.
    public O365CalendarModel.O365Calendar_Event createEvent(String subject)
    {
        // The com.microsoft.office365.OutlookServices.Event is created
        // and cached in the event model
        Event newEvent = new Event();
        return new O365CalendarModel.O365Calendar_Event(subject, newEvent);
    }

    // This overload is called when Event objects are retrieved from the OutlookServices
    // endpoint.
    public O365CalendarModel.O365Calendar_Event createEvent(String id, Event event)
    {
        // The event model caches an existing com.microsoft.office365.OutlookServices.Event
        return new O365CalendarModel.O365Calendar_Event(id, event);
    }

    // Posts changes made to an existing event.
    public void postUpdatedEvent(final Activity activity,
            final O365CalendarModel.O365Calendar_Event eventToUpdate)
    {
        Event event = eventToUpdate.getEvent();

        if (event.getEnd().before(event.getStart()))
        {
            OperationResult opResult = new OperationResult(
                    "Update event"
                    , "Event was not updated. End cannot be before start."
                    , "-1");

            mEventOperationCompleteListener.onOperationComplete(opResult);
            return;
        }

        ListenableFuture<Event> updatedEvent = mApplication.getCalendarClient()
                .getMe()
                .getEvents()
                .getById(event.getId())
                .update(event);

        Futures.addCallback(updatedEvent, new FutureCallback<Event>() {

            @Override
            public void onSuccess(final Event result)
            {
                // Notify caller that the Event update operation is complete and succeeded
                OperationResult opResult = new OperationResult(
                        "Update event"
                        , "Event updated"
                        , eventToUpdate.id);

                eventToUpdate.thisEvent = result;
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }

            @Override
            public void onFailure(final Throwable t)
            {
                Log.e(t.getMessage(), "Update event");
                // Notify caller that the operation failed
                OperationResult opResult = new OperationResult(
                        "Update event"
                        , "Event was not updated: " + getErrorMessage(t.getMessage())
                        , "-1");

                mEventOperationCompleteListener.onOperationComplete(opResult);
            }
        });
    }

    // Posts an event deletion
    @SuppressWarnings("unchecked")
    public void postDeletedEvent(final Activity activity,
            final O365CalendarModel.O365Calendar_Event eventToDelete)
    {
        if (eventToDelete == null)
        {
            OperationResult opResult = new OperationResult(
                    "Remove event"
                    , "Select an event before clicking the Delete event button "
                    , "-1");

            mEventOperationCompleteListener.onOperationComplete(opResult);
            return;
        }
        String eventId = eventToDelete.getEvent().getId();
        ListenableFuture<Event> deletedEvent = mApplication.getCalendarClient()
                .getMe()
                .getEvents()
                .getById(eventId).delete();

        Futures.addCallback(deletedEvent, new FutureCallback<Event>() {

            @Override
            public void onSuccess(final Event result)
            {
                // Remove event from calendar events collection. This collection is
                // the source of the ArrayAdapter attached to the event list in the UI
                mCalendarEvents.ITEMS.remove(eventToDelete);
                mCalendarEvents.ITEM_MAP.remove(eventToDelete.id);

                OperationResult opResult = new OperationResult(
                        "Remove event"
                        , "Removed event"
                        , eventToDelete.id);

                mEventOperationCompleteListener.onOperationComplete(opResult);
            }

            @Override
            public void onFailure(final Throwable t)
            {
                Log.e(t.getMessage(), "Delete event");
                OperationResult opResult = new OperationResult(
                        "Remove event"
                        , "Remove event failed: " + getErrorMessage(t.getMessage())
                        , "-1");

                mEventOperationCompleteListener.onOperationComplete(opResult);
            }
        });
    }

    // Posts a new event
    public void postCreatedEvent(final Activity activity,
            final O365CalendarModel.O365Calendar_Event eventToAdd)
    {
        try
        {
            Event newEvent = eventToAdd.getEvent();

            if (newEvent.getEnd().before(newEvent.getStart()))
            {
                OperationResult opResult = new OperationResult(
                        "Add event"
                        , "Event was not added. End cannot be before start."
                        , "-1");

                mEventOperationCompleteListener.onOperationComplete(opResult);
                return;
            }

            // This request returns the user's primary calendar. if you want to get
            // a different calendar in the user's calendar collection in Office 365,
            //
            ListenableFuture<Event> addedEvent = mApplication.getCalendarClient()
                    .getMe()
                    .getCalendars()
                    .getById(Constants.CALENDER_ID)
                    .getEvents().add(newEvent);

            Futures.addCallback(addedEvent, new FutureCallback<Event>()
            {

                @Override
                public void onSuccess(final Event result)
                {
                    OperationResult opResult = new OperationResult(
                            "Add event"
                            , "Added event"
                            , result.getId());

                    eventToAdd.thisEvent = result;
                    mEventOperationCompleteListener.onOperationComplete(opResult);
                }

                @Override
                public void onFailure(final Throwable t)
                {
                    Log.e(t.getMessage(), "Create event");
                    OperationResult opResult = new OperationResult(
                            "Add event"
                            , "Event was not added: " + getErrorMessage(t.getMessage())
                            , "-1");

                    mEventOperationCompleteListener.onOperationComplete(opResult);
                }
            });
        } catch (NullPointerException npe)
        {
            Log.e("Null pointer on add new event in O365CalendarModel.postCreatedEvent : "
                    + npe.getMessage()
                    , "null pointer");

            OperationResult opResult = new OperationResult(
                    "Add event"
                    , "Event was not added"
                    , "-1");

            mEventOperationCompleteListener.onOperationComplete(opResult);
        }
    }

    public void getEventList()
    {
        if (mCalendarEvents == null)
            mCalendarEvents = new CalendarEvents();

        // retrieve primary calendar events asynchronously
        ListenableFuture<List<Event>> results = mApplication.getCalendarClient()
                .getMe()
                .getCalendars().getById(Constants.CALENDER_ID)
                .getEvents()
                .read();

        Futures.addCallback(results, new FutureCallback<List<Event>>() {

            @Override
            public void onSuccess(final List<Event> result)
            {
                loadEventsIntoModel(result);
                setEventCollection eventData = new setEventCollection(mCalendarEvents.ITEMS);
                mEventSelectionListener.OnEventsAdded(eventData);
            }

            @Override
            public void onFailure(final Throwable t)
            {
                Log.e("Failed to get events: " +  getErrorMessage(t.getMessage()), "O365CalendarModel.getEventList");
                setEventCollection eventData = new setEventCollection(mCalendarEvents.ITEMS);
                mEventSelectionListener.OnEventsAdded(eventData);
            }
        });
        return;
    }

    private String getErrorMessage(String result)
    {
        String errorMessage = "";
        try {
            String responsejSON = result
                    .substring(result.indexOf("{"),result.length()); 
            JSONObject jObject = new JSONObject(responsejSON);

            JSONObject error =  (JSONObject) jObject.get("error");
            errorMessage = error.getString("message");

        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        return errorMessage;
    }
    
    private void loadEventsIntoModel(List<Event> events)
    {
        try
        {
            this.getCalendar().ITEMS.clear();
            this.getCalendar().ITEM_MAP.clear();
            for (Event e : events)
            {
                O365Calendar_Event calendarEvent = this.createEvent(e.getId(), e);
                ItemBody itemBody = e.getBody();
                if (itemBody != null)
                    calendarEvent.setItemBody(e.getBody());

                Location location = e.getLocation();
                if (location != null)
                    calendarEvent.setLocation(e.getLocation());

                java.util.Calendar startDate = e.getStart();
                java.util.Calendar endDate = e.getEnd();

                calendarEvent.setStartDate(
                        startDate.get(Calendar.YEAR)
                        , startDate.get(Calendar.MONTH)
                        , startDate.get(Calendar.DAY_OF_MONTH)
                        , startDate.get(Calendar.HOUR_OF_DAY)
                        , startDate.get(Calendar.MINUTE));

                calendarEvent.setEndDate(
                        endDate.get(Calendar.YEAR)
                        , endDate.get(Calendar.MONTH)
                        , endDate.get(Calendar.DAY_OF_MONTH)
                        , endDate.get(Calendar.HOUR_OF_DAY)
                        , endDate.get(Calendar.MINUTE));

                calendarEvent.setSubject(e.getSubject());
                addItem(calendarEvent);
            }
        } catch (Exception ex)
        {
            String exceptionMessage = ex.getMessage();
            Log.e("RetrieveEventsTask", exceptionMessage);
        }
    }

    private void addItem(O365Calendar_Event item) {
        this.getCalendar().ITEMS.add(item);
        this.getCalendar().ITEM_MAP.put(item.id, item);
    }

    public O365CalendarModel(Activity activity)
    {
        mApplication = (O365APIsStart_Application) activity.getApplication();
    }

    /**
     * Helper class for providing content for user interfaces created by Android template wizards.
     */
    public class CalendarEvents {
        public List<O365Calendar_Event> ITEMS = new ArrayList<O365Calendar_Event>();
        public Map<String, O365Calendar_Event> ITEM_MAP = new HashMap<String, O365Calendar_Event>();
    }

    /**
     * A single calendar event. The class exposes event properties as simple strings
     */
    public class O365Calendar_Event {
        public String id;
        public String content;
        String subject = " ";
        String attendees = "";
        String locationString = "";
        ItemBody itemBody;
        String itemBodyString = "";
        private Location location;
        Calendar endDateTime;
        Event thisEvent;

        // Sets the subject property of an event and
        // sets the event item body (content) with the
        // same subject string
        public void setSubject(String Subject)
        {
            subject = Subject;
            if (this.itemBody != null)
            {
                content = subject;
                this.itemBody.setContent(Subject);
                this.itemBody.setContentType(BodyType.Text);
                thisEvent.setSubject(Subject);
            }
        }

        // Updates the subject of the event
        public void updateSubject(String Subject)
        {
            subject = Subject;
            if (thisEvent != null)
                thisEvent.setSubject(Subject);
        }

        // Returns the subject of the event
        public String getSubject()
        {
            String returnValue = "";
            if (thisEvent != null)
            {
                returnValue = subject;
            }
            else
            {
                returnValue = thisEvent.getSubject();
            }
            return returnValue;
        }

        // Returns a comma delimited list of attendee
        // email addresses
        public String getAttendees()
        {
            try
            {
                // Get any previously invited attendees
                if (thisEvent.getAttendees() != null)
                {
                    attendees = "";
                    List<Attendee> attendeeList = thisEvent.getAttendees();
                    for (Attendee a : attendeeList)
                    {
                        String charSeparator = "";
                        String attendeeName = a.getEmailAddress().getName();

                        if (attendeeList.size() > 1)
                            charSeparator = "; ";

                        // New Attendee objects that have not been posted to Outlook service
                        // do not have resolved name objects. In this case, use the Address which
                        // is always filled.
                        if (attendeeName == null)
                            attendeeName = a.getEmailAddress().getAddress();

                        String newName = attendeeName;
                        attendees += newName + charSeparator;
                    }

                    // Trim off trailing space and the comma that trails the invitee list
                    attendees = attendees.trim();
                    if (attendees.endsWith(";"))
                    {
                        attendees = attendees.substring(0, attendees.length() - 1);
                    }
                }
            } catch (Exception ex)
            {
                Log.e("Exception on get attendees: " + ex.getMessage(), "");
            }
            return attendees;
        }

        // Add new attendees to the existing list of event attendees
        public void setAttendees(String anAttendee)
        {
            // If the event has no attendees, just add the new list.
            if (thisEvent.getAttendees() == null || thisEvent.getAttendees().isEmpty())
            {
                String[] attendeeArray = anAttendee.split(";");
                for (String attendeeString : attendeeArray)
                {
                    if (attendeeString.contains("@") && attendeeString.contains("."))
                        makeAnAttendee(attendeeString.trim());
                }
            }
            else
            {
                // iterate the array of attendee names from the user's list on the
                // UI and compare each string with the existing attendee list. If
                // the name is not in the list, add the new string as an invitee

                // The comma delimited list of attendees from UI
                String[] attendeeArray = anAttendee.split(";");

                // The existing attendee list from the event
                List<Attendee> attendeeList = thisEvent.getAttendees();
                checkRemoveAttendeFromList(anAttendee, attendeeList);

                // Iterate on attendee array
                for (String attendeeString : attendeeArray)
                {
                    if (attendeeString.contains("@") && attendeeString.contains("."))
                        checkAddAttendeeToEvent(attendeeString, attendeeList);
                }
            }
        }

        // Add an attendee to the event attendee collection if it is not already there
        private void checkAddAttendeeToEvent(String attendeeToAdd, List<Attendee> attendeeList)
        {
            attendeeToAdd = attendeeToAdd.trim();
            Boolean inList = false;

            // Iterate on attendee list
            for (Attendee a : attendeeList)
            {
                if (!attendeeToAdd.contains("@") && a.getEmailAddress().getName() != null)
                {
                    // The Name property of an event attendee is null if the
                    // Attendee object was created with a person name instead of
                    // a resolvable email address
                    if (a.getEmailAddress().getName().trim().equals(attendeeToAdd.trim()))
                    {
                        inList = true;
                        break;
                    }
                }
                else
                {
                    if (a.getEmailAddress().getAddress().equals(attendeeToAdd))
                    {
                        inList = true;
                        break;
                    }
                }
            }
            if (inList == false)
            {
                makeAnAttendee(attendeeToAdd);
            }
        }

        // Any names removed from the attendee list on the UI are removed from the attendee
        // collection
        // on the event.
        private void checkRemoveAttendeFromList(String attendeNames, List<Attendee> attendeeList)
        {
            List<Attendee> attendeesToRemove = new ArrayList<Attendee>();

            // Iterate on the Attendee collection in the Event and add Event objects
            // to be removed from the Attendee collection
            for (Attendee attendee : attendeeList)
            {
                String attendeeName = attendee.getEmailAddress().getName();

                // If the name of the attendee is not in the comma delimited
                // list of attendee names from the UI then add to the
                // to-be-removed collection
                if (!attendeNames.contains(attendeeName))
                {
                    attendeesToRemove.add(attendee);
                }
            }

            if (!attendeesToRemove.isEmpty())
            {
                for (Attendee attendeeToRemove : attendeesToRemove)
                {
                    thisEvent.getAttendees().remove(attendeeToRemove);
                }
            }

        }

        private void makeAnAttendee(String anAttendee)
        {
            // Works for new attendee added to event
            this.attendees = anAttendee;
            Attendee attendee1 = new Attendee();
            EmailAddress email = new EmailAddress();
            email.setAddress(anAttendee);
            attendee1.setEmailAddress(email);
            List<Attendee> listAttendees = thisEvent.getAttendees();
            if (listAttendees == null)
                listAttendees = new ArrayList<Attendee>();
            listAttendees.add(attendee1);
            thisEvent.setAttendees(listAttendees);
        }

        // Sets the location of an event
        public void setLocation(Location location)
        {
            locationString = location.getDisplayName();
            this.location = location;
        }

        // Sets the location in the OutlookServices event object
        public void setLocation(String Location)
        {
            locationString = Location;
            if (this.location != null)
            {
                this.location.setDisplayName(Location);
                thisEvent.setLocation(this.location);
            }
            else
            {
                Location newLocation = new Location();
                newLocation.setDisplayName(Location);
                thisEvent.setLocation(newLocation);
            }
        }

        public void setItemBody(ItemBody body)
        {
            this.itemBody = body;
            this.itemBodyString = body.getContent();
        }

        public String getItemBody()
        {
            return itemBodyString;
        }

        public String getLocation()
        {
            return locationString;
        }

        public void setStartDate(int yearValue, int monthValue, int dayValue, int hourValue,
                int minuteValue)
        {
            Calendar startDate = thisEvent.getStart();
            if (startDate == null)
            {
                startDate = new GregorianCalendar(
                        yearValue
                        , monthValue
                        , dayValue);
            }
            startDate.setTimeZone(TimeZone.getDefault());
            startDate.set(yearValue, monthValue, dayValue, hourValue, minuteValue);
            thisEvent.setStart(startDate);
        }

        public void setEndDate(int yearValue, int monthValue, int dayValue, int hourValue,
                int minuteValue)
        {
            Calendar endDate = thisEvent.getEnd();
            if (endDate == null)
            {
                endDate = new GregorianCalendar(
                        yearValue
                        , monthValue
                        , dayValue);
            }
            endDate.setTimeZone(TimeZone.getDefault());
            endDate.set(yearValue, monthValue, dayValue, hourValue, minuteValue);
            thisEvent.setEnd(endDate);
        }

        public Calendar getStartDateTime()
        {
            return thisEvent.getStart();
        }

        public Calendar getEndDateTime()
        {
            return thisEvent.getEnd();
        }

        public Event getEvent()
        {
            return thisEvent;
        }

        public O365Calendar_Event(String id, Event event) {
            this.id = id;
            thisEvent = event;

        }

        public O365Calendar_Event(String id)
        {
            this.id = id;
            thisEvent = new Event();
            thisEvent.setId(this.id);
        }

        // the toString override is called by the two-pane list box to show
        // calendar event details in the list.
        @Override
        public String toString() {
            String amPm = "AM ";
            if (thisEvent.getStart().get(Calendar.AM_PM) == 1)
                amPm = "PM ";

            int startHour = thisEvent.getStart().get(Calendar.HOUR);
            String hourString = Integer.toString(startHour);

            int startMinute = thisEvent.getStart().get(Calendar.MINUTE);
            String minuteString = Integer.toString(startMinute);
            if (startMinute < 10)
                minuteString = "0" + minuteString;

            return (thisEvent.getStart().get(Calendar.MONTH) + 1)
                    + "/"
                    + thisEvent.getStart().get(Calendar.DAY_OF_MONTH)
                    + " "
                    + hourString
                    + ":"
                    + minuteString
                    + " "
                    + amPm
                    + subject;
        }
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