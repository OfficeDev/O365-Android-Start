/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Email;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;
import com.microsoft.office365.starter.helpers.Constants;
import com.microsoft.office365.starter.helpers.ProgressDialogHelper;
import com.microsoft.office365.starter.interfaces.BaseDialogListener;
import com.microsoft.office365.starter.interfaces.OnMessagesAddedListener;
import com.microsoft.office365.starter.interfaces.OnOperationCompleteListener;
import com.microsoft.office365.starter.helpers.DeleteDialogFragment;

/**
 * An activity representing a list of MailItems. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link com.microsoft.office365.starter.Email.MailItemDetailActivity}
 * representing item details. On tablets, the activity presents the list of
 * items and item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MailItemListFragment} and the item details (if present) is a
 * {@link com.microsoft.office365.starter.Email.MailItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MailItemListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class MailItemListActivity extends Activity implements
		OnMessagesAddedListener, BaseDialogListener,
		OnOperationCompleteListener, MailItemListFragment.Callbacks,
		MailItemComposeFragment.Callbacks {

	DeleteDialogFragment mDeleteFragment;
	ProgressDialog mDialog;
	O365APIsStart_Application mApplication;
	private ArrayAdapter<O365MailItemsModel.O365Mail_Message> mListAdapter;
	private int mMessagePageNumber;
	private int mMessagePageSize = 11;
	private android.app.FragmentManager mFragmentManager;
	private static final String COMPOSE_FRAGMENT_STACK_STATE = "composeFragment";
	private String mSelectedMailItemID = null;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mailitem_list);

		this.setTitle(R.string.title_section4);
		if (findViewById(R.id.mailitem_detail_container) != null) {
			mTwoPane = true;

			((MailItemListFragment) getFragmentManager().findFragmentById(
					R.id.mailitem_list)).setActivateOnItemClick(true);
		}

		mApplication = (O365APIsStart_Application) getApplication();
		O365MailItemsModel model = new O365MailItemsModel(this);
		mApplication.setMailItemsModel(model);
		model.setMessageOperationCompleteListener(this);
		mApplication.getMailItemsModel().setMessageAddedListener(this);

		actionGetMail();
		MailItemListFragment listFragment = (MailItemListFragment) getFragmentManager()
				.findFragmentById(R.id.mailitem_list);

		ArrayAdapter<O365MailItemsModel.O365Mail_Message> listAdapter = new ArrayAdapter<O365MailItemsModel.O365Mail_Message>(
				this, android.R.layout.simple_list_item_activated_1,
				mApplication.getMailItemsModel().getMail().ITEMS);

		mListAdapter = listAdapter;
		listFragment.setListAdapter(listAdapter);
		mFragmentManager = getFragmentManager();

		if (mTwoPane) {
			if (mTwoPane == true) {
				// Load the overview text into the WebView
				WebView introView = (WebView) findViewById(R.id.EmailStarterTextWebView);
				introView.setBackgroundColor(getResources().getColor(
						R.color.ApplicationPageBackgroundThemeBrush));
				String introHTML = getResources().getString(
						R.string.email_view_intro);
				introView.loadData(introHTML, "text/html", "UTF-8");
				introView.setVisibility(View.VISIBLE);
			}

		}
	}

	public void actionGetMail() {

		mDialog = ProgressDialogHelper.showProgressDialog(
				MailItemListActivity.this, "Getting email from server...",
				"Please wait.");

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		mApplication.getMailItemsModel().getMessageList(mMessagePageSize,
				mMessagePageNumber);
		mSelectedMailItemID = null;
	}

	// Opens create event fragment and swaps with current event detail fragment
	private void actionCreateNewMessage() {
		if (mTwoPane == true) {
			MailItemComposeFragment createFragment = new MailItemComposeFragment();
			android.app.FragmentTransaction ft = mFragmentManager
					.beginTransaction();
			ft.replace(R.id.mailitem_detail_container, createFragment,
					COMPOSE_FRAGMENT_STACK_STATE);

			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(COMPOSE_FRAGMENT_STACK_STATE);
			ft.commit();
		} else {
			Intent detailIntent = new Intent(this, MailItemDetailActivity.class);
			detailIntent.setAction(Intent.ACTION_INSERT);
			startActivity(detailIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mail_detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.message_new:
			// check to be sure an email isn't already being composed by seeing
			// if fragment exists
			if (getFragmentManager().findFragmentByTag(
					COMPOSE_FRAGMENT_STACK_STATE) == null)
				actionCreateNewMessage();

			else
				Toast.makeText(
						this,
						"Cancel or send the current email before starting a new one.",
						Toast.LENGTH_LONG).show();

			break;

		case R.id.message_refresh:
			actionGetMail();
			break;

		case R.id.message_delete:

			// check that we aren't in the middle of composing a new email.
			if (getFragmentManager().findFragmentByTag(
					COMPOSE_FRAGMENT_STACK_STATE) == null)
				actionDeleteMessage();

			else
				Toast.makeText(this,
						"Cancel or send the current email before deleting.",
						Toast.LENGTH_LONG).show();

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void DisplayMailItemFragment(String id) {
		if (id == null)
			return;

		Bundle arguments = new Bundle();
		arguments.putString(MailItemDetailFragment.ARG_ITEM_ID, id);
		MailItemDetailFragment fragment = new MailItemDetailFragment();
		fragment.setArguments(arguments);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.mailitem_detail_container, fragment,
						Constants.MAIL_DETAIL_FRAGMENT_TAG).commit();

	}

	/**
	 * Callback method from {@link MailItemListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		mSelectedMailItemID = id;
		if (mTwoPane
				&& getFragmentManager().findFragmentByTag(
						COMPOSE_FRAGMENT_STACK_STATE) == null)

			// Check to see if the compose fragment is already displayed
			// in which case don't display the selected mail contents
			// since that will erase the new email in progress
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			DisplayMailItemFragment(id);

		else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, MailItemDetailActivity.class);
			detailIntent.putExtra(MailItemDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	// Callback from model indicating messages were retrieved and added locally
	// to the list from the server
	@Override
	public void OnMessagesAdded(final MessageCollection messageCollection) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (mDialog.isShowing())
					mDialog.dismiss();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

				// Remove detail fragment if it is displayed from a previous
				// email read
				MailItemDetailFragment mailDetailFragment = (MailItemDetailFragment) getFragmentManager()
						.findFragmentByTag(Constants.MAIL_DETAIL_FRAGMENT_TAG);
				if (mailDetailFragment != null)
					getFragmentManager().beginTransaction()
							.detach(mailDetailFragment).commit();

				MailItemListFragment mailListFragment = (MailItemListFragment) getFragmentManager()
						.findFragmentById(R.id.mailitem_list);
				((ArrayAdapter<O365MailItemsModel.O365Mail_Message>) mailListFragment
						.getListAdapter()).notifyDataSetChanged();

				mailListFragment.getListView().setVisibility(View.VISIBLE);
				mailListFragment.setListAdapter(mListAdapter);

				if (messageCollection.getMessageCollection().isEmpty())
					Toast.makeText(MailItemListActivity.this,
							"No messages to show", Toast.LENGTH_LONG).show();

				else
					Toast.makeText(MailItemListActivity.this,
							"Messages retrieved", Toast.LENGTH_LONG).show();

			}
		});
	}

	// Callback from model indicating when an API operation is completed
	@Override
	public void onOperationComplete(final OperationResult opResult) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mDialog.isShowing())
					mDialog.dismiss();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

				Toast.makeText(MailItemListActivity.this,
						opResult.getOperationResult(), Toast.LENGTH_LONG)
						.show();
				MailItemListFragment mailItemListFragment = (MailItemListFragment) getFragmentManager()
						.findFragmentById(R.id.mailitem_list);

				// Notify the list adaptor that the underlying event list has
				// changed
				((ArrayAdapter<O365MailItemsModel.O365Mail_Message>) mailItemListFragment
						.getListAdapter()).notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onSendMail(String mailTo, String mailCc, String mailSubject,
			String mailBody) {
		mApplication.getMailItemsModel().postNewMailToServer(mailTo, mailCc,
				mailSubject, mailBody);
		if (mTwoPane) {
			FragmentManager fm = getFragmentManager();
			fm.popBackStack(COMPOSE_FRAGMENT_STACK_STATE,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	@Override
	public void onSendMailCancelled() {
		if (mTwoPane) {
			FragmentManager fm = getFragmentManager();
			fm.popBackStack(COMPOSE_FRAGMENT_STACK_STATE,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);

			DisplayMailItemFragment(mSelectedMailItemID);
		}
	}

	private void actionDeleteMessage() {
		if (mSelectedMailItemID == null) {
			Toast.makeText(MailItemListActivity.this,
					"Select an email to delete", Toast.LENGTH_LONG).show();
			return;
		}
		O365MailItemsModel.O365Mail_Message mailMessage = mApplication
				.getMailItemsModel().getMail().ITEM_MAP
				.get(mSelectedMailItemID);

		Bundle arguments = new Bundle();
		arguments.putString("MessageString",
				"Delete " + mailMessage.getSubject() + "?");
		mDeleteFragment = new DeleteDialogFragment();
		mDeleteFragment.setArguments(arguments);
		mDeleteFragment.show(mFragmentManager, "Delete this email?");
	}

	@Override
	public void onDialogPositiveClick(Fragment dialog) {
		if (dialog == mDeleteFragment) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

			mDialog = new ProgressDialog(this);
			mDialog.setTitle("Deleting the email...");
			mDialog.setMessage("Please wait.");
			mDialog.setCancelable(true);
			mDialog.setIndeterminate(true);
			mDialog.show();
			O365MailItemsModel.O365Mail_Message mailMessage = mApplication
					.getMailItemsModel().getMail().ITEM_MAP
					.get(mSelectedMailItemID);

			mApplication.getMailItemsModel().postDeleteMailItem(
					mailMessage.getID());
		}
	}

	@Override
	public void onDialogNegativeClick(Fragment dialog) {
		// No action needed
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
