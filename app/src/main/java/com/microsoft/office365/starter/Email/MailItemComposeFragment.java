/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Email;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.office365.starter.R;

public class MailItemComposeFragment extends Fragment {

	String mMailTo;
	String mMailCc;
	String mMailSubject;
	String mMailBody;

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
		public void onSendMail(String mailTo, String mailCc,
				String mailSubject, String mailBody);

		public void onSendMailCancelled();
	}

	public MailItemComposeFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Callbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					activity.toString()
							+ " must implement Mail Compose Fragment Callbacks interface");
		}

	}

	private void sendMessage(View rootView) {
		// Get data from fields on the view
		TextView textView = (TextView) rootView
				.findViewById(R.id.mail_compose_to);
		mMailTo = textView.getText().toString();
		textView = (TextView) rootView.findViewById(R.id.mail_compose_cc);
		mMailCc = textView.getText().toString();
		textView = (TextView) rootView.findViewById(R.id.mail_compose_subject);
		mMailSubject = textView.getText().toString();
		textView = (TextView) rootView.findViewById(R.id.mail_compose_body);
		mMailBody = textView.getText().toString();

		// Inform host activity which will remove this fragment and send email
		mListener.onSendMail(mMailTo, mMailCc, mMailSubject, mMailBody);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(
				R.layout.fragment_mailitem_compose, container, false);

		// Keyboard send button handler
		EditText editText = (EditText) rootView
				.findViewById(R.id.mail_compose_body);
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendMessage(rootView);
					handled = true;
				}
				return handled;
			}
		});

		// Done button click event handler
		rootView.findViewById(R.id.actionbar_done).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						sendMessage(rootView);
					}
				});

		// Cancel button click event handler
		rootView.findViewById(R.id.actionbar_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// Call parent activity listener to remove this
						// fragment.
						mListener.onSendMailCancelled();
						mListener = null;
					}
				});
		return rootView;
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
