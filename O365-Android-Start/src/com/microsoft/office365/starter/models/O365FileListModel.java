/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.models;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.fileservices.Item;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener.OperationResult;
import com.microsoft.sharepointservices.odata.SharePointClient;

public class O365FileListModel {

    private O365APIsStart_Application mApplication;
    private int selectedItem = -1;

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int value) {
        selectedItem = value;
    }

    private OnOperationCompleteListener mEventOperationCompleteListener;

    public O365FileListModel(O365APIsStart_Application value)
    {
        mApplication = value;
    }

    public void setEventOperationCompleteListener(
            OnOperationCompleteListener eventOperationCompleteListener)
    {
        this.mEventOperationCompleteListener = eventOperationCompleteListener;
    }

    public void postUpdatedFileContents(final O365APIsStart_Application application,
            final Activity currentActivity, SharePointClient fileClient)
    {
        ListenableFuture<Void> future = fileClient
                .getfiles()
                .getById(application.getDisplayedFile()
                        .getId())
                .asFile()
                .putContent(application
                        .getDisplayedFile()
                        .getContents()
                        .getBytes());

        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                Log.e("Asset", t.getMessage());
                // Notify caller that the Event update operation is complete and succeeded
                OperationResult opResult = new OperationResult(
                        "Post updated file contents"
                        , "failed: " + getErrorMessage(t.getMessage())
                        , "");
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }

            @Override
            public void onSuccess(Void v) {
                // Notify caller that the Event update operation is complete and succeeded
                OperationResult opResult = new OperationResult(
                        "Post updated file contents"
                        , "Posted updated file contents"
                        , "");
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }
        });

    }

    private String getErrorMessage(String result)
    {
        String errorMessage = "";
        try {
            String responseString = result;
            String responsejSON = responseString
                    .substring(responseString.indexOf("{"),responseString.length()); 
            JSONObject jObject = new JSONObject(responsejSON);

            JSONObject error =  (JSONObject) jObject.get("error");
            errorMessage = error.getString("message");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        return errorMessage;
    }
    
    public void postNewFileToServer(final O365APIsStart_Application application,
            final Activity currentActivity, String fileName, final String fileContents,
            final SharePointClient fileClient)
    {
        final Item newFile = new Item();
        newFile.settype("File");
        newFile.setname(fileName);
        ListenableFuture<Item> future = fileClient.getfiles().add(newFile);
        Futures.addCallback(future, new FutureCallback<Item>() {
            @Override
            public void onFailure(Throwable t) {
                Log.e("Asset", t.getMessage());
                // Notify caller that the Event update operation is complete and succeeded

                OperationResult opResult = new OperationResult(
                        "Post new file "
                        , "Failed: " + getErrorMessage(t.getMessage())
                        , "");
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }

            @Override
            public void onSuccess(final Item item) {
                try {
                    fileClient.getfiles().getById(item.getid()).asFile()
                            .putContent(fileContents.getBytes()).get();
                    currentActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            application.getFileListViewState().addNewFileToList(item);
                        }
                    });
                    // Notify caller that the Event update operation is complete and succeeded
                    OperationResult opResult = new OperationResult(
                            "Post new file to server"
                            , "Posted new file to server"
                            , "");
                    mEventOperationCompleteListener.onOperationComplete(opResult);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void addNewFileToList(Item newFile)
    {
        O365FileModel newFileModel = new O365FileModel(mApplication, newFile);
        mApplication.getFileAdapterList().add(newFileModel);
        mApplication.getFileAdapterList().notifyDataSetChanged();
    }

    private void deleteSelectedFileFromList(int index)
    {
        if (index < 0)
            return;
        O365FileModel itemToRemove = mApplication.getFileAdapterList().getItem(index);
        mApplication.getFileAdapterList().remove(itemToRemove);
        selectedItem = -1;
        mApplication.getFileAdapterList().notifyDataSetChanged();
    }

    public void postDeleteSelectedFileFromServer(final Activity currentActivity,
            SharePointClient fileClient)
    {
        if (-1 == selectedItem)
        {
            // Notify caller that the Event update operation is complete and succeeded
            OperationResult opResult = new OperationResult(
                    "Post delete selected file "
                    , "failed: No file selected to delete"
                    , "");
            mEventOperationCompleteListener.onOperationComplete(opResult);
            return;
        }
        final int index = selectedItem; // cache this for the return trip thread before reset at end
                                        // of this method
        O365FileModel fileToDelete = (O365FileModel) mApplication.getFileAdapterList().getItem(
                index);

        final O365APIsStart_Application application = (O365APIsStart_Application) currentActivity
                .getApplication();
        ListenableFuture future = fileClient
                .getfiles()
                .getById(fileToDelete
                        .getId())
                .asFile()
                .addHeader("If-Match", "*")
                .delete();

        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onFailure(Throwable t) {
                Log.e("Asset", t.getMessage());
                // Notify caller that the Event update operation is complete and succeeded
                OperationResult opResult = new OperationResult(
                        "Post delete selected file "
                        , "failed: " + getErrorMessage(t.getMessage())
                        , "");
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }

            @Override
            public void onSuccess(Object obj) {
                currentActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        application.getFileListViewState().deleteSelectedFileFromList(index);
                    }
                });
                // Notify caller that the Event update operation is complete and succeeded
                OperationResult opResult = new OperationResult(
                        "Post delete selected file on server"
                        , "Posted delete selected file on server"
                        , "");
                mEventOperationCompleteListener.onOperationComplete(opResult);
            }
        });
        selectedItem = -1;
    }

    public void getFilesAndFoldersFromService(final Activity currentActivity,
            SharePointClient fileClient)
    {
        final ArrayList<O365FileModel> fileList = new ArrayList<O365FileModel>();
        ListenableFuture<List<Item>> future = fileClient
                .getfiles()
                .read();

        Futures.addCallback(future,
                new FutureCallback<List<Item>>() {
                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Asset", t.getMessage());
                        // Notify caller that the Event update operation is complete and succeeded
                        OperationResult opResult = new OperationResult(
                                "Get folders and files"
                                , "failed: " + getErrorMessage(t.getMessage())
                                , "");
                        mEventOperationCompleteListener.onOperationComplete(opResult);
                    }

                    @Override
                    public void onSuccess(List<Item> files) {

                        for (Item item : files) {
                            Log.i("file: ", item.toString());
                            fileList.add(new O365FileModel(mApplication, item));
                        }

                        // we're not on the UI thread right now, so call back
                        // to the UI thread to update the ListView and set text
                        currentActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mApplication.getFileAdapterList().clear();
                                for (O365FileModel f : fileList)
                                {
                                    mApplication.getFileAdapterList().add(f);
                                }
                                mApplication.getFileAdapterList().notifyDataSetChanged();

                            }
                        });
                        // Notify caller that the Event update operation is complete and succeeded
                        OperationResult opResult = new OperationResult(
                                "Get folders and files"
                                , "Got folders and files"
                                , "");
                        mEventOperationCompleteListener.onOperationComplete(opResult);
                    }

                });
    }

    public O365FileModel getFileContentsFromServer(final Activity currentActivity)
    {
        if (-1 == selectedItem)
        {
            // Notify caller that the Event update operation is complete and succeeded
            OperationResult opResult = new OperationResult(
                    "Get file contents"
                    , "failed: No file selected"
                    , "");
            mEventOperationCompleteListener.onOperationComplete(opResult);
            return null;
        }
        O365FileModel selectedFile = (O365FileModel) mApplication.getFileAdapterList().getItem(
                selectedItem);
        final O365FileModel fm = new O365FileModel(mApplication, selectedFile.getItem());
        String fileName = fm.getName();
        if (fileName != null && (fileName.contains(".txt") || fileName.contains(".xml")))
        {
            ListenableFuture<byte[]> future = mApplication
                    .getFileClient()
                    .getfiles()
                    .getById(selectedFile
                            .getId())
                    .asFile()
                    .getContent();

            Futures.addCallback(future,
                    new FutureCallback<byte[]>() {
                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("Asset", t.getMessage());
                            // Notify caller that the Event update operation is complete and
                            // succeeded
                            OperationResult opResult = new OperationResult(
                                    "Get file contents"
                                    , "failed: " + getErrorMessage(t.getMessage())
                                    , "");
                            mEventOperationCompleteListener.onOperationComplete(opResult);
                        }

                        @Override
                        public void onSuccess(final byte[] fileBytes) {
                            currentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try
                                    {
                                        fm.setContents(currentActivity, new String(fileBytes,
                                                "UTF-8"));
                                    }
                                    catch (UnsupportedEncodingException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            // Notify caller that the Event update operation is complete and
                            // succeeded
                            OperationResult opResult = new OperationResult(
                                    "Get file contents"
                                    , "Got file contents"
                                    , "");
                            mEventOperationCompleteListener.onOperationComplete(opResult);
                        }
                    });
            mApplication.setDisplayedFile(fm);
            return fm;
        }
        else
        {
            // Notify caller that the Event update operation is complete and succeeded
            OperationResult opResult = new OperationResult(
                    "Get file contents"
                    , "Select a .txt or .xml file to read"
                    , "");
            mEventOperationCompleteListener.onOperationComplete(opResult);
            return null;
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