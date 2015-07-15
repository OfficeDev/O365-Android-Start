/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.FilesFolders;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.interfaces.OnFileChangedEventListener;

/**
 * A fragment representing a single File detail screen. This fragment is
 * either contained in a {@link FileListActivity} in two-pane mode (on tablets)
 * or a {@link com.microsoft.office365.starter.FilesFolders.FileDetailActivity} on handsets.
 */
public class FileDetailFragment extends Fragment implements
		OnFileChangedEventListener {
	private String mContents = null;
	private O365APIsStart_Application mApplication = null;
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FileDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (O365APIsStart_Application) getActivity()
				.getApplication();
		if (mApplication.getDisplayedFile() != null)
			mContents = mApplication.getDisplayedFile().getContents();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_file_detail,
				container, false);

		// Show the file content as text in a TextView.
		if (mContents != null)
			((TextView) rootView.findViewById(R.id.file_detail))
					.setText(mContents);


		return rootView;
	}

	// Called by parent activity when a new file is being displayed
	public void refresh(O365FileModel fileItem) {
		onFileChangedEvent(fileItem, null);
	}

	// When file contents are changed, or new file was read from server, render
	// the new contents to the TextView.
	@Override
	public void onFileChangedEvent(O365FileModel fileItem, Event event) {
		String fileContents;
		if (fileItem == null)
			fileContents = "";
		 else
			fileContents = fileItem.getContents();

		TextView textView = (TextView) getView().findViewById(
				R.id.file_detail);
		textView.setText(fileContents);
		mApplication.setDisplayedFile(fileItem);
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
