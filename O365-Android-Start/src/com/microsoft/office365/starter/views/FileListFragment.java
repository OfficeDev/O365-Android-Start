/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.microsoft.fileservices.Item;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.models.O365FileModel;

public class FileListFragment extends Fragment {

    private FilesActivity parentActivity;
    private O365APIsStart_Application mApplication;
    private ArrayAdapter<O365FileModel> mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mApplication = (O365APIsStart_Application) getActivity().getApplication();
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // store this for accessing shared state that the activity contains
        parentActivity = (FilesActivity) activity;
    }

    public void initialize()
    {
        ArrayList<O365FileModel> starterList = new ArrayList<O365FileModel>();
        ListView listView = (ListView) getActivity().findViewById(R.id.list_files);
        mListAdapter = new ArrayAdapter<O365FileModel>(getActivity(),
                android.R.layout.simple_list_item_1, starterList);
        listView.setAdapter(mListAdapter);
        mApplication = (O365APIsStart_Application) getActivity().getApplication();
        mApplication.setFileAdapterList(mListAdapter);
    }

    public OnItemClickListener mMessageClickedHandler = new OnItemClickListener()
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            mApplication.getFileListViewState().setSelectedItem(position);
        }
    };
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