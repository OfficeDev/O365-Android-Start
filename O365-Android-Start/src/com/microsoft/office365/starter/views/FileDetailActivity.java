/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.helpers.ProgressDialogHelper;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;

/**
 * An activity representing a single File detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link FileListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link FileDetailFragment}.
 */
public class FileDetailActivity extends Activity implements
		FileUpdateFragment.Callbacks, OnOperationCompleteListener {

	private static final String UPDATE_FRAGMENT_STACK_STATE = "updateFragment";
	private ProgressDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			FileDetailFragment fragment = new FileDetailFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.file_detail_container, fragment).commit();

		}
	}

	// Handler for update action from button press
	private void updateActionHandler() {
		// Display the update fragment
		FileUpdateFragment updateFragment = new FileUpdateFragment();
		FragmentManager fragmentManager = getFragmentManager();
		android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.file_detail_container, updateFragment,
				UPDATE_FRAGMENT_STACK_STATE);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(UPDATE_FRAGMENT_STACK_STATE);
		ft.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.file_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_file_update:
			updateActionHandler();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, FileListActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onContentsUpdated(String updatedContent) {
		FragmentManager fm = getFragmentManager();
		fm.popBackStack(UPDATE_FRAGMENT_STACK_STATE,
				FragmentManager.POP_BACK_STACK_INCLUSIVE);

		O365APIsStart_Application application = (O365APIsStart_Application) getApplication();
		application.getDisplayedFile().setContents(FileDetailActivity.this,
				updatedContent);
		mDialog = ProgressDialogHelper.showProgressDialog(
				FileDetailActivity.this, "Updating file contents on server...",
				"Please wait.");
		application.getFileListViewState().setEventOperationCompleteListener(
				FileDetailActivity.this);
		application.getFileListViewState().postUpdatedFileContents(application,
				FileDetailActivity.this, application.getFileClient(),
				updatedContent);
	}

	@Override
	public void onFileUpdateCancelled() {
		FragmentManager fm = getFragmentManager();
		fm.popBackStack(UPDATE_FRAGMENT_STACK_STATE,
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public void onOperationComplete(final OperationResult opResult) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mDialog.isShowing()) {
					mDialog.dismiss();
				}

				Toast.makeText(FileDetailActivity.this,
						opResult.getOperationResult(), Toast.LENGTH_LONG)
						.show();
				if (opResult.getId().equals("FileDeleted")) {
					// The file displayed may be the one just deleted
					// so clear the display to be safe.
					O365APIsStart_Application application = (O365APIsStart_Application) getApplication();
					application.setDisplayedFile(null);
					FragmentManager fm = getFragmentManager();
					FileDetailFragment fragment = (FileDetailFragment) fm
							.findFragmentById(R.id.file_detail_container);
					if (fragment != null)
						fragment.refresh(null);
				}
				if (opResult.getId().equals("FileContentsUpdate")) {
					// refresh the display to reflect new file contents
					FragmentManager fm = getFragmentManager();
					FileDetailFragment fragment = (FileDetailFragment) fm
							.findFragmentById(R.id.file_detail_container);
					if (fragment != null)
					{
						O365APIsStart_Application application = (O365APIsStart_Application) getApplication();
						fragment.refresh(application.getDisplayedFile());
					}
				}


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