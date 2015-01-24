/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.MainActivity;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.interfaces.MainActivityCoordinator;


public class MainButtonsFragment extends Fragment implements OnClickListener {

	private ImageButton mCalendarButton;
	private ImageButton mFilesButton;
    private ImageButton mMailButton;
	private O365APIsStart_Application mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (O365APIsStart_Application) getActivity()
				.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_main_buttons,
				container, false);

		// Create references to ImageButtons
		mCalendarButton = (ImageButton) fragmentView
				.findViewById(R.id.calendarButton);
		mFilesButton = (ImageButton) fragmentView
				.findViewById(R.id.filesButton);
        mMailButton = (ImageButton) fragmentView
                .findViewById(R.id.mailButton);
		mCalendarButton.setOnClickListener(this);
		mFilesButton.setOnClickListener(this);
        mMailButton.setOnClickListener(this);

		// Inflate the layout for this fragment
		return fragmentView;
	}

	public void setButtonsEnabled(boolean enabled) {
		setImageButtonEnabled(getActivity(), enabled, mCalendarButton,
				R.drawable.calendar_icon_main);
		setImageButtonEnabled(getActivity(), enabled, mFilesButton,
				R.drawable.myfiles_icon_main);
        setImageButtonEnabled(getActivity(), enabled, mMailButton,
                R.drawable.mail_icon_main);
	}

	private static void setImageButtonEnabled(Context context, boolean enabled,
			ImageButton item, int iconResId) {
		item.setEnabled(enabled);
		item.setClickable(enabled);
		Drawable originalIcon = context.getResources().getDrawable(iconResId);
		int overlay = context.getResources().getColor(R.color.DisabledItemBrush);
		Drawable icon = enabled ? originalIcon : applyOverlayToDrawable(
				originalIcon, overlay);
		item.setImageDrawable(icon);
	}

	private static Drawable applyOverlayToDrawable(Drawable drawable,
			int overlay) {
		if (drawable == null) {
			return null;
		}
		Drawable res = drawable.mutate();
		res.setColorFilter(overlay, Mode.SRC_IN);
		return res;
	}

	@Override
	public void onClick(View v) {
		String capability = "";
		switch (v.getId()) {
		case R.id.filesButton:
			capability = Constants.MYFILES_CAPABILITY;
			break;
		case R.id.calendarButton:
			capability = Constants.CALENDAR_CAPABILITY;
			if (mApplication.getCalendarModel() != null) {
				mApplication.getCalendarModel().getCalendar().ITEMS.clear();
				mApplication.getCalendarModel().getCalendar().ITEM_MAP.clear();
			}
			break;
        case R.id.mailButton:
            capability = Constants.MAIL_CAPABILITY;
            break;
		}

        Log.i("Office 365 Starter Project", "User selected the " + capability + " capability");
        MainActivityCoordinator mainActivity = (MainActivityCoordinator) getActivity();
        mainActivity.onServiceSelected(capability);
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