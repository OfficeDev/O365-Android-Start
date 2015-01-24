/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Email;

import android.app.Activity;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.helpers.APIErrorMessageHelper;
import com.microsoft.office365.starter.interfaces.OnMessagesAddedListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.outlookservices.BodyType;
import com.microsoft.outlookservices.EmailAddress;
import com.microsoft.outlookservices.ItemBody;
import com.microsoft.outlookservices.Message;
import com.microsoft.outlookservices.Recipient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class O365MailItemsModel
{

    private MailMessages mMailMessages;
    private UUID tempNewMessageId;
    private O365APIsStart_Application mApplication;
    private OnMessagesAddedListener mMessageAddedListener;
    private OnOperationCompleteListener mMessageOperationCompleteListener;


    public O365MailItemsModel(Activity activity)
    {
        if (activity == null)
        {
            return;
        }
        mApplication = (O365APIsStart_Application) activity.getApplication();
    }


    public void setMessageAddedListener(
            OnMessagesAddedListener messagesAddedListener)
    {
        this.mMessageAddedListener = messagesAddedListener;
    }

    public void setMessageOperationCompleteListener(
            OnOperationCompleteListener messageOperationCompleteListener)
    {
        this.mMessageOperationCompleteListener = messageOperationCompleteListener;
    }

    // Returns an instance of the MailMessages class after constructing it if necessary
    public MailMessages getMail()
    {
        if (mMailMessages == null)
        {
            mMailMessages = new MailMessages();
        }
        return mMailMessages;
    }

    // This overload is called when Message objects are retrieved from the OutlookServices
    // endpoint.
    public O365Mail_Message createMessage(String id, Message message)
    {
        // The message model caches an existing com.microsoft.office365.OutlookServices.Message
        return new O365Mail_Message(id, message);
    }

    // This overload is called when a user is creating a new message.
    public O365Mail_Message createMessage(String subject)
    {
        // Create a temporary unique message Id for the new message. The
        // temporary Id is used by the ListView to uniquely id the new message
        // when it is added to the local cache before posting to the Outlook service
        UUID ID = java.util.UUID.randomUUID();

        // Cache the temp Id in the message model so the model can retrieve the
        // message out of the ITEMS_MAP map and update with the Id assigned by
        // Outlook service upon successful add
        tempNewMessageId = ID;

        // The com.microsoft.office365.OutlookServices.message is created
        // and cached in the message model
        Message newMessage = new Message();
        O365Mail_Message newMessageModel = new O365Mail_Message(subject, newMessage);
        newMessageModel.setID(ID.toString());
        return newMessageModel;
    }

    private boolean isEmailAddress(String emailAddress)
    {
        boolean returnValue = false;
        Pattern pattern;
        Matcher matcher;

        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        // Add mail to address if mailToString is an email address
        matcher = pattern.matcher(emailAddress);
        if (matcher.matches())
        {
            returnValue = true;
        }
        return returnValue;
    }

    private Recipient MakeARecipient(String mailAddress)
    {
        Recipient recipient = null;
        if (isEmailAddress(mailAddress))
        {
            recipient = new Recipient();
            EmailAddress email = new EmailAddress();
            email.setName(mailAddress);
            email.setAddress(mailAddress);
            recipient.setEmailAddress(email);
        }
        return recipient;

    }

    public void postDeleteMailItem(final String messageToDeleteID)
    {

        ListenableFuture<Void> results = mApplication.getMailClient()
                .getMe()
                .getMessages()
                .getById(
                        this.getMail()
                                .ITEM_MAP
                                .get(messageToDeleteID)
                                .getID()
                )
                .delete();

        Futures.addCallback(
                results, new FutureCallback<Void>()
                {
                    @Override
                    public void onSuccess(Void v)
                    {
                        //Remove the deleted mail from the local object model
                        //list and map
                        O365MailItemsModel
                                .this
                                .getMail()
                                .ITEMS
                                .remove(
                                        O365MailItemsModel
                                                .this
                                                .getMail()
                                                .ITEM_MAP
                                                .get(messageToDeleteID)
                                );

                        O365MailItemsModel
                                .this
                                .getMail()
                                .ITEM_MAP
                                .remove(messageToDeleteID);

                        //send notification
                        OnOperationCompleteListener.OperationResult eventData = new OnOperationCompleteListener.OperationResult(
                                "Delete Mail"
                                , "Mail message was successfully deleted."
                                , null
                        );
                        mMessageOperationCompleteListener.onOperationComplete(eventData);
                    }

                    @Override
                    public void onFailure(final Throwable t)
                    {
                        Log.e(
                                "Failed to get messages: " + APIErrorMessageHelper.getErrorMessage(t.getMessage()),
                                "O365MailItemsModel.postDeleteMailItem"
                        );
                        OnOperationCompleteListener.OperationResult eventData = new OnOperationCompleteListener.OperationResult(
                                "Delete Mail"
                                , "An error occurred attempting to delete the Mail message."
                                , null
                        );
                        mMessageOperationCompleteListener.onOperationComplete(eventData);
                    }
                }
        );


    }


    public void postNewMailToServer(String mailTo, String mailCc, String mailSubject, String mailBody)
    {
        //Set recipients
        List<Recipient> emailAddresses = new ArrayList<Recipient>();
        List<Recipient> ccEmailAddresses = new ArrayList<Recipient>();

        //Split the mailTo string into individual mail to
        //email addresses, validate each email address,
        //and add valid email addresses to the List<Recipient> array.
        String[] mailToArray = mailTo.split(";");
        for (String mailToString : mailToArray)
        {
            if (isEmailAddress(mailToString))
            {
                emailAddresses.add(MakeARecipient(mailToString));
            }

        }
        //Split the mailTo string into individual mail to
        //email addresses, validate each email address,
        //and add valid email addresses to the List<Recipient> array.
        String[] mailCcArray = mailCc.split(";");
        for (String mailCcString : mailCcArray)
        {
            if (isEmailAddress(mailCcString))
            {
                ccEmailAddresses.add(MakeARecipient(mailCcString));
            }

        }

        //If at least one of the mail to strings is a valid email address
        // send the email message with all valid email addresses
        if (!emailAddresses.isEmpty())
        {
            Message messageToSend = new Message();
            messageToSend.setToRecipients(emailAddresses);

            if (!ccEmailAddresses.isEmpty())
            {
                messageToSend.setCcRecipients(ccEmailAddresses);
            }

            ItemBody body = new ItemBody();
            body.setContent(mailBody);
            messageToSend.setBody(body);
            messageToSend.setSubject(mailSubject);

            ListenableFuture<Integer> results = mApplication
                    .getMailClient()
                    .getMe()
                    .getOperations()
                    .sendMail(messageToSend, true);

            Futures.addCallback(
                    results, new FutureCallback<Integer>()
                    {
                        @Override
                        public void onSuccess(Integer result)
                        {
                            OnOperationCompleteListener.OperationResult eventData = new OnOperationCompleteListener.OperationResult(
                                    "Send Mail"
                                    , "New Mail message sent successfully."
                                    , ""
                            );
                            mMessageOperationCompleteListener.onOperationComplete(eventData);

                        }

                        @Override
                        public void onFailure(Throwable t)
                        {
                            Log.e(
                                    "Failed to get messages: " + APIErrorMessageHelper.getErrorMessage(
                                            t.getMessage()
                                    ),
                                    "O365MailItemsModel.postNewMailToServer"
                            );
                            OnOperationCompleteListener.OperationResult eventData = new OnOperationCompleteListener.OperationResult(
                                    "Send Mail"
                                    ,
                                    "An error occurred sending the Mail message. Check the error log."
                                    ,
                                    null
                            );
                            mMessageOperationCompleteListener.onOperationComplete(eventData);

                        }
                    }
            );

        }
        //If no mail to strings are valid email addresses, invoke the operation complete method with
        //the fail state
        else
        {
            OnOperationCompleteListener.OperationResult eventData = new OnOperationCompleteListener.OperationResult(
                    "Send Mail"
                    , "An error occurred because one or more emails were not formatted correctly."
                    , null
            );
            mMessageOperationCompleteListener.onOperationComplete(eventData);

        }
    }


    //Get a set of email messages, starting with the message at skipTomessageNumber
    //Size of message set is set by pageSize
    public void getMessageList(int pageSize, int skipToMessageNumber)
    {
        if (mMailMessages == null)
        {
            mMailMessages = new MailMessages();
        }

        try
        {
            // retrieve a page of email messages asynchronously
            ListenableFuture<List<Message>> results = mApplication.getMailClient()
                    .getMe()
                    .getFolders().getById("Inbox")
                    .getMessages()
                    .top(pageSize)
                    .orderBy("DateTimeReceived desc")
                    .read();

            Futures.addCallback(
                    results, new FutureCallback<List<Message>>()
                    {

                        @Override
                        public void onSuccess(final List<Message> result)
                        {
                            loadMessagesIntoModel(result);
                            OnMessagesAddedListener.MessageCollection MessageItemData = new OnMessagesAddedListener
                                    .MessageCollection(mMailMessages.ITEMS);

                            mMessageAddedListener.OnMessagesAdded(MessageItemData);
                        }

                        @Override
                        public void onFailure(final Throwable t)
                        {
                            Log.e(
                                    "Failed to get messages: " + APIErrorMessageHelper.getErrorMessage(
                                            t.getMessage()
                                    ),
                                    "O365MailItemsModel.getMessageList"
                            );
                            OnMessagesAddedListener.MessageCollection eventData = new OnMessagesAddedListener
                                    .MessageCollection(mMailMessages.ITEMS);
                            mMessageAddedListener.OnMessagesAdded(eventData);
                        }
                    }
            );
        }
        catch (Exception ex)
        {
            String exceptionMessage = ex.getMessage();
            Log.e("RetrieveMessagesTask", exceptionMessage);
        }
    }


    private void loadMessagesIntoModel(List<Message> message)
    {
        try
        {
            this.getMail().ITEMS.clear();
            this.getMail().ITEM_MAP.clear();
            for (Message m : message)
            {
                O365Mail_Message mailMessage = this.createMessage(m.getId(), m);
                ItemBody itemBody = m.getBody();
                if (itemBody != null)
                {
                    mailMessage.setItemBody(m.getBody());
                }

                mailMessage.setSubject(m.getSubject());
                addItem(mailMessage);
            }
        }
        catch (Exception ex)
        {
            String exceptionMessage = ex.getMessage();
            Log.e("RetrievemessagesTask", exceptionMessage);
        }
    }

    private void addItem(O365Mail_Message item)
    {
        this.getMail()
                .ITEMS
                .add(item);
        this.getMail()
                .ITEM_MAP
                .put(item.id, item);
    }

    /**
     * Helper class for providing content for user interfaces created by Android template wizards.
     */
    public class MailMessages
    {
        public List<O365Mail_Message> ITEMS = new ArrayList<O365Mail_Message>();
        public Map<String, O365Mail_Message> ITEM_MAP = new HashMap<String, O365Mail_Message>();
    }

    /**
     * A single mail message. The class exposes message properties as simple strings
     */
    public class O365Mail_Message
    {
        private String id;
        private String subject = " ";
        private String recipients = "";
        private String ccRecipients = "";
        private ItemBody itemBody;
        private String itemBodyString = "";
        private Message thisMessage;

        // Sets the subject property of a message and
        // sets the message item body (content) with the
        // same subject string
        public void setSubject(String Subject)
        {
            subject = Subject;
            if (this.itemBody != null)
            {
                this.itemBody.setContent(Subject);
                this.itemBody.setContentType(BodyType.Text);
                thisMessage.setSubject(Subject);
            }
        }

        public void setMessage(Message message)
        {
            thisMessage = message;
            this.id = message.getId();
        }

        // Updates the subject of the message
        public void updateSubject(String Subject)
        {
            subject = Subject;
            if (thisMessage != null)
            {
                thisMessage.setSubject(Subject);
            }
        }

        // Returns a comma delimited list of recipient
        // email addresses
        public String getMessageRecipients()
        {
            try
            {
                // Get any previously invited attendees
                if (thisMessage.getToRecipients() != null)
                {
                    recipients = makeRecipientsList(thisMessage.getToRecipients());
                }
            }
            catch (Exception ex)
            {
                Log.e("Exception on get recipients: " + ex.getMessage(), "");
            }
            return recipients;
        }

        public String getCCMessageRecipients()
        {
            try
            {
                // Get any CC recipients
                if (thisMessage.getCcRecipients() != null)
                {
                    ccRecipients = makeRecipientsList(thisMessage.getCcRecipients());
                }
            }
            catch (Exception ex)
            {
                Log.e("Exception on get CC recipients: " + ex.getMessage(), "");
            }
            return ccRecipients;
        }

        private String makeRecipientsList(List<Recipient> recipients)
        {
            String recipientString = "";
            for (Recipient r : recipients)
            {
                String charSeparator = "";
                String recipientName = r.getEmailAddress().getAddress();

                if (recipients.size() > 1)
                {
                    charSeparator = ";";
                }

                recipientString += recipientName + charSeparator;
            }

            // Trim off trailing space and the comma that trails the recipient list
            recipientString = recipientString.trim();
            if (recipientString.endsWith(";"))
            {
                recipientString = recipientString.substring(0, recipientString.length() - 1);
            }

            return recipientString;
        }


        // Returns the email address of the sender
        public String getFrom()
        {
            return thisMessage
                    .getFrom()
                    .getEmailAddress()
                    .getAddress();
        }

        // Returns the subject of the message
        public String getSubject()
        {
            String returnValue = "";
            if (thisMessage == null)
            {
                returnValue = subject;
            }

            else if (thisMessage.getSubject() != null)
            {
                returnValue = thisMessage.getSubject();
            }

            return returnValue;
        }

        public String getID()
        {
            return this.id;
        }

        public void setID(String newId)
        {
            id = newId;
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

        public Message getMessage()
        {
            return thisMessage;
        }

        // Add new recipients to the existing list of message recipients
        public void setMessageRecipients(String recipients)
        {
            if (thisMessage.getToRecipients() != null)
            {
                thisMessage.getToRecipients().clear();
            }


            Pattern pattern;
            Matcher matcher;

            String EMAIL_PATTERN =
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            pattern = Pattern.compile(EMAIL_PATTERN);

            String[] recipientArray = recipients.split(";");
            for (String recipientString : recipientArray)
            {
                // Add recipient if recipientString is an email address
                matcher = pattern.matcher(recipientString);
                if (matcher.matches())
                {
                    makeARecipient(recipientString.trim());
                }
            }
        }

        private void makeARecipient(String aRecipient)
        {
            this.recipients = aRecipient;
            Recipient recipient = new Recipient();
            EmailAddress email = new EmailAddress();
            email.setAddress(aRecipient);
            recipient.setEmailAddress(email);

            // Get the current list of message recipients and add the new recipient
            // to the list
            List<Recipient> listRecipients = thisMessage.getToRecipients();
            if (listRecipients == null)
            {
                listRecipients = new ArrayList<Recipient>();
            }
            listRecipients.add(recipient);
            thisMessage.setToRecipients(listRecipients);
        }


        public O365Mail_Message(String id, Message message)
        {
            this.id = id;
            thisMessage = message;

        }

        public O365Mail_Message(String id)
        {
            this.id = id;
            thisMessage = new Message();
            thisMessage.setId(this.id);
        }

        // the toString override is called by the two-pane list box to show
        // Email message details in the list.
        @Override
        public String toString()
        {

            Calendar sentDateCalendar = thisMessage.getDateTimeSent();
            String sentDate = sentDateCalendar.get(Calendar.MONTH) + 1
                    + "/"
                    + sentDateCalendar.get(Calendar.DAY_OF_MONTH)
                    + "/"
                    + sentDateCalendar.get(Calendar.YEAR);

            return thisMessage.getSender().getEmailAddress().getName()
                    + "\n"
                    + thisMessage.getSubject()
                    + "\n"
                    + sentDate;
        }
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
