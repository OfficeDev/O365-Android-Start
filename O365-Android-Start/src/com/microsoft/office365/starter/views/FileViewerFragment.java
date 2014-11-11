/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.R.id;
import com.microsoft.office365.starter.R.layout;
import com.microsoft.office365.starter.interfaces.OnFileChangedEventListener;
import com.microsoft.office365.starter.interfaces.OnFileChangedEventListener.Event;
import com.microsoft.office365.starter.models.O365FileModel;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FileViewerFragment extends Fragment implements OnFileChangedEventListener {

    private boolean isDisplayingContents = false;

    public boolean getIsDisplayingContents() {
        return isDisplayingContents;
    }

    public void reset()
    {
        isDisplayingContents = false;
        TextView fileContentsView = (TextView) getActivity().findViewById(id.file_contents_preview);
        EditText fileContentsEditor = (EditText) getActivity().findViewById(id.file_contents_edit);
        fileContentsView.setText(null);
        fileContentsEditor.setText(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_file_viewer, container, false);
    }

    private void setTextOfDisplayedFileView(String contents)
    {
        TextView fileContentsView = (TextView) getActivity().findViewById(id.file_contents_preview);
        EditText fileContentsEditor = (EditText) getActivity().findViewById(id.file_contents_edit);
        if (fileContentsView == null || fileContentsEditor == null)
            return;
        if (fileContentsView.getVisibility() != View.GONE)
            fileContentsView.setText(contents);
        else
            fileContentsEditor.setText(contents);
    }

    @Override
    public void onFileChangedEvent(O365FileModel fileItem, Event event) {
        String fileContents = fileItem.getContents();
        setTextOfDisplayedFileView(fileContents);
        isDisplayingContents = true;
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