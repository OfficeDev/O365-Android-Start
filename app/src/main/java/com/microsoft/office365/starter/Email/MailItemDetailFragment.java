package com.microsoft.office365.starter.Email;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.microsoft.office365.starter.O365APIsStart_Application;
import com.microsoft.office365.starter.R;

/**
 * A fragment representing a single MailItem detail screen. This fragment is
 * either contained in a {@link MailItemListActivity} in two-pane mode (on
 * tablets) or a
 * {@link com.microsoft.office365.starter.Email.MailItemDetailActivity} on
 * handsets.
 */
public class MailItemDetailFragment extends Fragment {

	private O365APIsStart_Application mApplication;
	private O365MailItemsModel mMailItems;

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private O365MailItemsModel.O365Mail_Message mMailItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MailItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (O365APIsStart_Application) getActivity()
				.getApplication();
		if (getArguments().containsKey(ARG_ITEM_ID))
			// Load the mail item content specified by the fragment
			// arguments.
			mMailItem = mApplication.getMailItemsModel().getMail().ITEM_MAP
					.get(getArguments().getString(ARG_ITEM_ID));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_mailitem_detail,
				container, false);

		// Show the mail item content as text in a TextView.
		if (mMailItem != null) {
			String daString;
			TextView editTo = (TextView) rootView
					.findViewById(R.id.mail_detail_to);
			editTo.setText(mMailItem.getMessageRecipients());
			TextView editCC = (TextView) rootView
					.findViewById(R.id.mail_detail_cc);
			editCC.setText(mMailItem.getCCMessageRecipients());
			TextView editFrom = (TextView) rootView
					.findViewById(R.id.mail_detail_from);
			editFrom.setText(mMailItem.getFrom());
			TextView editSubject = (TextView) rootView
					.findViewById(R.id.mail_detail_subject);
			editSubject.setText(mMailItem.getSubject());
			WebView editBody = (WebView) rootView
					.findViewById(R.id.mail_detail_body);
			editBody.loadData(mMailItem.getItemBody(), "text/html", "UTF-8");
		}

		return rootView;
	}
}
