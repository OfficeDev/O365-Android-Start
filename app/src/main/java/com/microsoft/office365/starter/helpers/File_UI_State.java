/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.helpers;

import android.view.MenuItem;
import android.widget.Button;

//Helper class to manage UI for enabling and disabling buttons
//based on UI state.
public class File_UI_State {

	public boolean isListItemSelected = true;
	public boolean isEditing = true;
	public boolean isFileContentsDisplayed = true;

	public Button btnGet = null;
	public Button btnCreate = null;
	public Button btnDelete = null;
	public Button btnRead = null;
	public Button btnUpdate = null;

	public MenuItem itemGet = null;
	public MenuItem itemCreate = null;
	public MenuItem itemDelete = null;
	public MenuItem itemRead = null;
	public MenuItem itemUpdate = null;
    public MenuItem itemUpload = null;
    public MenuItem itemDownload = null;

	public void setEditMode(boolean mode) {
		if (mode == isEditing)
			return;
		isEditing = mode;
		if (btnGet != null)
			btnGet.setEnabled(!isEditing);
		if (btnCreate != null)
			btnCreate.setEnabled(!isEditing);
		if (btnDelete != null)
			btnDelete.setEnabled(!isEditing);
		if (btnRead != null)
			btnRead.setEnabled(!isEditing);
		if (btnUpdate != null)
			btnUpdate.setEnabled(!isEditing);

		if (itemGet != null)
			itemGet.setEnabled(!isEditing);
		if (itemCreate != null)
			itemCreate.setEnabled(!isEditing);
		if (itemDelete != null)
			itemDelete.setEnabled(!isEditing);
		if (itemRead != null)
			itemRead.setEnabled(!isEditing);
		if (itemUpdate != null)
			itemUpdate.setEnabled(!isEditing);
        if (itemUpload != null)
            itemUpload.setEnabled(!isEditing);
        if (itemDownload != null)
            itemDownload.setEnabled(!isEditing);

	}

	public void setFileDisplayMode(boolean mode) {
		if (mode == isFileContentsDisplayed)
			return;
		isFileContentsDisplayed = mode;
		if (btnUpdate != null)
			btnUpdate.setEnabled(isFileContentsDisplayed);

		if (itemUpdate != null)
			itemUpdate.setEnabled(isFileContentsDisplayed);

	}

	public void setListSelectedMode(boolean mode) {
		if (mode == isListItemSelected)
			return;
		isListItemSelected = mode;
		if (btnDelete != null)
			btnDelete.setEnabled(isListItemSelected);
		if (btnRead != null)
			btnRead.setEnabled(isListItemSelected);

		if (itemDelete != null)
			itemDelete.setEnabled(isListItemSelected);
		if (itemRead != null)
			itemRead.setEnabled(isListItemSelected);

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