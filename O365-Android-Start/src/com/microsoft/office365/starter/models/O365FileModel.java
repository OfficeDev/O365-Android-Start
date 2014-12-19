/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.models;

import android.app.Activity;
import com.microsoft.fileservices.Item;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.interfaces.OnFileChangedEventListener;

public class O365FileModel {

	private OnFileChangedEventListener eventFileChangedListener;
	private Item fileItem = null;
	private String fileType = null;

	public String getFileType() {
		return fileType;
	}

	public Item getItem() {
		return fileItem;
	}

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String value) {
		if (value != null)
			id = value;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		if (value != null)
			name = value;
	}

	private String contents;

	public String getContents() {
		return contents;
	}

	public void setContents(Activity currentActivity, String value) {
		contents = value;
		fireContentsChanged(currentActivity);
	}

	public void setFileChangedEventListener(OnFileChangedEventListener event) {
		this.eventFileChangedListener = event;

	}

	public O365FileModel(O365APIsStart_Application application, Item newFile) {
		name = newFile.getname();
		id = newFile.getid();
		fileItem = newFile;
		fileType = newFile.gettype();
	}

	@Override
	public String toString() {
		return name;
	}

	private void fireContentsChanged(Activity currentActivity) {
		if (eventFileChangedListener != null) {
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						OnFileChangedEventListener.Event event = new OnFileChangedEventListener.Event();
						event.setEventType(OnFileChangedEventListener.Event.eventType.contentsChanged);
						eventFileChangedListener.onFileChangedEvent(
								O365FileModel.this, event);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
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