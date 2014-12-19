/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.R.id;
import com.microsoft.office365.starter.R.layout;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class FileUpdateFragment extends Fragment {

	private String mContents = null;
	private Callbacks mListener;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onContentsUpdated(String updatedContent);

		public void onFileUpdateCancelled();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		O365APIsStart_Application application = (O365APIsStart_Application) getActivity()
				.getApplication();
		application = (O365APIsStart_Application) getActivity()
				.getApplication();
		if (application.getDisplayedFile() != null)
			mContents = application.getDisplayedFile().getContents();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_file_detail_update,
				container, false);
		final EditText editView = (EditText) rootView
				.findViewById(R.id.file_detail_update);
		if (editView != null && mContents != null) {
			editView.setText(mContents);
		}

		// Done button click event handler
		rootView.findViewById(R.id.actionbar_done).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mContents = editView.getText().toString();

						// Call parent activity listener to post updated
						// contents to server.
						mListener.onContentsUpdated(mContents);
					}
				});

		// Cancel button click event handler
		rootView.findViewById(R.id.actionbar_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						// Call parent activity listener to remove this
						// fragment.
						mListener.onFileUpdateCancelled();
						mListener = null;
					}
				});

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Callbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					activity.toString()
							+ " must implement File Update Fragment Callbacks interface");
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
