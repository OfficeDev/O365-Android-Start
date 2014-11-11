/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.BaseDialogListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.models.O365CalendarModel.O365Calendar_Event;
import com.microsoft.office365.starter.models.O365CalendarModel;
import com.microsoft.office365.starter.models.O365FileListModel;
import com.microsoft.office365.starter.models.O365FileModel;

public class FilesActivity extends Activity implements BaseDialogListener,
        OnOperationCompleteListener {

    private O365APIsStart_Application mApplication;
    private boolean isEditing; // true if the user is editing file contents; otherwise false;
    private ListView fileListView;
    private DeleteDialogFragment mDeleteDialog;
    /** The m stored rotation. */
    private int mStoredRotation;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        fileListView = (ListView) findViewById(R.id.list_files);

        FragmentManager fm = getFragmentManager();
        FileListFragment listFragment = (FileListFragment) fm
                .findFragmentById(R.id.file_list_fragment);
        listFragment.initialize();
            TextView fileContents = (TextView) this.findViewById(R.id.file_contents_preview);
            fileContents.setOnClickListener(mClickedHandler);
       
        fileListView.setOnItemClickListener(listFragment.mMessageClickedHandler);

        mApplication = (O365APIsStart_Application) getApplication();
        mApplication.setFileListViewState(new O365FileListModel(mApplication));
        isEditing = false;

        Button fileButton = (Button) this.findViewById(R.id.button_fileget);
        fileButton.setOnClickListener(mGetClickedHandler);
        fileButton = (Button) this.findViewById(R.id.button_filecreate);
        fileButton.setOnClickListener(mCreateClickedHandler);
        fileButton = (Button) this.findViewById(R.id.button_filedelete);
        fileButton.setOnClickListener(mDeleteClickedHandler);
        fileButton = (Button) this.findViewById(R.id.button_fileread);
        fileButton.setOnClickListener(mReadClickedHandler);
        fileButton = (Button) this.findViewById(R.id.button_filupdate);
        fileButton.setOnClickListener(mUpdateClickedHandler);

        EditText fileContentsEditor = (EditText) this.findViewById(R.id.file_contents_edit);
        fileContentsEditor.setVisibility(View.GONE);

        // Load the overview text into the WebView
        WebView introView = (WebView) findViewById(R.id.fileStarterTextWebView);
        introView.setBackgroundColor(getResources().getColor(
                R.color.ApplicationPageBackgroundThemeBrush));
        String introHTML = getResources().getString(R.string.files_view_intro);
        introView.loadData(introHTML, "text/html", "UTF-8");
        introView.setVisibility(0);
    }

    public OnClickListener mReadClickedHandler = new OnClickListener()
    {

        @Override
        public void onClick(View v) {
            if (mApplication.getFileListViewState().getSelectedItem() == -1)
                return;
            setEditMode(false);
            mDialog = new ProgressDialog(FilesActivity.this);
            mDialog.setTitle("Getting file contents from server...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mApplication.getFileListViewState().setEventOperationCompleteListener(
                    FilesActivity.this);
            O365FileModel fileModel = mApplication.getFileListViewState()
                    .getFileContentsFromServer(FilesActivity.this);
            if (fileModel != null)
            {
                FileViewerFragment fragmentViewer = (FileViewerFragment) getFragmentManager()
                        .findFragmentById(R.id.file_viewer_fragment);
                fileModel.setFileChangedEventListener(fragmentViewer);
                mApplication.setDisplayedFile(fileModel);
            }
        }
    };

    public OnClickListener mUpdateClickedHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (!isEditing) return;
            
            EditText fileEditor = (EditText) FilesActivity.this
                    .findViewById(R.id.file_contents_edit);
            mApplication.getDisplayedFile().setContents(FilesActivity.this,
                    fileEditor.getText().toString());
            mDialog = new ProgressDialog(FilesActivity.this);
            mDialog.setTitle("Updating file contents on server...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mApplication.getFileListViewState().setEventOperationCompleteListener(
                    FilesActivity.this);
            mApplication.getFileListViewState()
                    .postUpdatedFileContents(mApplication, FilesActivity.this,
                            mApplication.getFileClient());
            setEditMode(false);
        }
    };

    // When text view containing file text is clicked, switch to the edit view.
    public void onFileContentsEdit(View view)
    {
        setEditMode(true);
    }

    // mode is true if editing is being turned on, and false if it is being turned off.
    public void setEditMode(boolean mode)
    {
        if (mode == isEditing)
            return;

        // double check that there are actually contents to edit before switching modes
        FileViewerFragment fileViewer = (FileViewerFragment) getFragmentManager().findFragmentById(
                R.id.file_viewer_fragment);
        if (fileViewer == null)
            return;
        if (!fileViewer.getIsDisplayingContents())
            return;

        isEditing = mode;

        if (isEditing)
        {
            TextView fileContentsPreview = (TextView) this.findViewById(R.id.file_contents_preview);
            fileContentsPreview.setVisibility(View.GONE);
            EditText fileContentsEditor = (EditText) this.findViewById(R.id.file_contents_edit);
            fileContentsEditor.setVisibility(View.VISIBLE);
            fileContentsEditor.setText(fileContentsPreview.getText());
        }
        else
        {
            TextView fileContentsPreview = (TextView) this.findViewById(R.id.file_contents_preview);
            fileContentsPreview.setVisibility(View.VISIBLE);
            EditText fileContentsEditor = (EditText) this.findViewById(R.id.file_contents_edit);
            fileContentsEditor.setVisibility(View.GONE);
            fileContentsPreview.setText(fileContentsEditor.getText());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public OnClickListener mCreateClickedHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v) {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
            Date date = new Date();
            final String fileContents = "Created at " + dateFormat.format(date);
            mDialog = new ProgressDialog(FilesActivity.this);
            mDialog.setTitle("Adding the new file on server...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mApplication.getFileListViewState().setEventOperationCompleteListener(
                    FilesActivity.this);
            mApplication.getFileListViewState()
                    .postNewFileToServer(mApplication, FilesActivity.this, "demo.txt",
                            fileContents, mApplication
                                    .getFileClient());
        }
    };
    public OnClickListener mDeleteClickedHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v) {
        	if (mApplication.getFileListViewState().getSelectedItem()==-1) return;
            Bundle arguments = new Bundle();
            arguments.putInt("Message", R.string.FileDeleteLabel);
            mDeleteDialog = new DeleteDialogFragment();
            mDeleteDialog.setArguments(arguments);
            mDeleteDialog.show(getFragmentManager(), "Delete this file?");
        }
    };

    public OnClickListener mGetClickedHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v) {
            setEditMode(false);
            FileViewerFragment fileViewer = (FileViewerFragment) FilesActivity.this
                    .getFragmentManager().findFragmentById(R.id.file_viewer_fragment);
            fileViewer.reset();
            mDialog = new ProgressDialog(FilesActivity.this);
            mDialog.setTitle("Getting folders and files from server...");
            mDialog.setMessage("Please wait.");
            mDialog.setCancelable(false);
            mDialog.setIndeterminate(true);
            mDialog.show();
            mApplication.getFileListViewState().setEventOperationCompleteListener(
                    FilesActivity.this);
            mApplication.getFileListViewState().getFilesAndFoldersFromService(FilesActivity.this,
                    mApplication.getFileClient());
        }
    };

    public OnClickListener mClickedHandler = new OnClickListener()
    {

        @Override
        public void onClick(View v) {

            setEditMode(true);
        }
    };

    @Override
    public void onDialogNegativeClick(Fragment dialog) {
        // no action needed
    }

    // Callback called by delete dialog fragment when user clicks the
    // Done button
    @Override
    public void onDialogPositiveClick(Fragment dialog)
    {
        ListView listFiles = (ListView) findViewById(R.id.list_files);
        mApplication.getFileListViewState().setSelectedItem(listFiles.getCheckedItemPosition());
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Deleting selected file from server...");
        mDialog.setMessage("Please wait.");
        mDialog.setCancelable(false);
        mDialog.setIndeterminate(true);
        mDialog.show();
        mApplication.getFileListViewState().setEventOperationCompleteListener(this);
        mApplication.getFileListViewState().postDeleteSelectedFileFromServer(FilesActivity.this,
                mApplication.getFileClient());
    }

    @Override
    public void onOperationComplete(final OperationResult opResult) {
        this.runOnUiThread(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if (mDialog.isShowing())
                {
                    mDialog.dismiss();
                    FilesActivity.this.setRequestedOrientation(mStoredRotation);
                }
                Toast.makeText(FilesActivity.this, opResult.getOperationResult(),
                        Toast.LENGTH_LONG).show();
            }
        });
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