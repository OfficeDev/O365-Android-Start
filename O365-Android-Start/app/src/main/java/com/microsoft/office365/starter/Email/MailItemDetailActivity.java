/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.Email;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.microsoft.office365.starter.R;

/**
 * An activity representing a single MailItem detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link MailItemListActivity}
 * .
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link MailItemDetailFragment}.
 */
public class MailItemDetailActivity extends Activity implements
		MailItemComposeFragment.Callbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mailitem_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//

		if (savedInstanceState == null) {
			// see if we are creating new email, or viewing existing.
			// determines which fragment we need to load.
			Intent intent = getIntent();
			String actionMode = intent.getAction();
			if (Intent.ACTION_INSERT == actionMode) {
				// display the compose fragment to compose a new email
				MailItemComposeFragment fragment = new MailItemComposeFragment();
				getFragmentManager().beginTransaction()
						.add(R.id.mailitem_detail_container, fragment).commit();

			} else {
				// display the detail view fragment to read an email

				// Create the detail fragment and add it to the activity
				// using a fragment transaction.
				Bundle arguments = new Bundle();
				arguments.putString(
						MailItemDetailFragment.ARG_ITEM_ID,
						getIntent().getStringExtra(
								MailItemDetailFragment.ARG_ITEM_ID));
				MailItemDetailFragment fragment = new MailItemDetailFragment();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
						.add(R.id.mailitem_detail_container, fragment).commit();

			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					MailItemListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSendMail(String mailTo, String mailCc, String mailSubject,
			String mailBody) {
		// send mail

		this.finish();
	}

	@Override
	public void onSendMailCancelled() {

		this.finish();
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
