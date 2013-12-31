package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.CameraActivity_;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerCreatedEvent;
import com.cloudjay.cjay.events.ContainerEditedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ItemLongClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_gate_export)
@OptionsMenu(R.menu.menu_gate_export)
public class GateExportListFragment extends SherlockFragment {
	
	private final static String LOG_TAG = "GateExportListFragment";

	private ArrayList<Operator> mOperators;
	private ArrayList<ContainerSession> mFeeds;
	private FunDapter<ContainerSession> mFeedsAdapter;

	private ContainerSession mSelectedContainerSession;

	@ViewById(R.id.container_list) 		ListView mFeedListView;
	@ViewById(R.id.search_edittext)		EditText mSearchEditText;
	@ViewById(R.id.add_button)			Button mAddButton;
	@ViewById(R.id.notfound_textview)	TextView mNotfoundTextView;

	@AfterViews
	void afterViews() {
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				search(arg0.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mOperators = (ArrayList<Operator>) DataCenter.getInstance()
				.getListOperators(getActivity());
		configureControls(mFeeds);
		initContainerFeedAdapter(mFeeds);
	}

	@OptionsItem(R.id.menu_upload)
	void uploadMenuItemSelected() {
		// TODO
	}

	@Click(R.id.add_button)
	void addButtonClicked() {
		// show add container dialog
		showContainerDetailDialog(
				getResources().getString(R.string.default_container_id), "",
				AddContainerDialog.CONTAINER_DIALOG_ADD);
	}

	@ItemClick(R.id.container_list)
	void listItemClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, false);

		// clear current selection
		mSelectedContainerSession = null;
		getActivity().supportInvalidateOptionsMenu();

		// get the selected container session
		ContainerSession containerSession = mFeedsAdapter.getItem(position);
		TmpContainerSession tmpContainerSession = Mapper.toTmpContainerSession(
				containerSession, getActivity());

		// Pass tmpContainerSession away
		// Then start showing the Camera
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				tmpContainerSession);
		intent.putExtra("type", CJayImage.TYPE_EXPORT); // out
		startActivity(intent);
	}

	@ItemLongClick(R.id.container_list)
	void listItemLongClicked(int position) {
		// refresh highlighting
		mFeedListView.setItemChecked(position, true);

		// refresh menu
		mSelectedContainerSession = mFeedsAdapter.getItem(position);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean isDisplayed = !(mSelectedContainerSession == null);
		menu.findItem(R.id.menu_upload).setVisible(isDisplayed);
	}
	
	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}
	
	public void showContainerDetailDialog(String containerId,
			String operatorName, int mode) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog();
		addContainerDialog.setContainerId(containerId);
		addContainerDialog.setOperatorName(operatorName);
		addContainerDialog.setMode(mode);
		addContainerDialog.setParent(this);
		addContainerDialog.show(fm, "add_container_dialog");
	}

	public void OnOperatorSelected(String containerId, String operatorName,
			int mode) {
		showContainerDetailDialog(containerId, operatorName, mode);
	}

	public void OnContainerInputCompleted(String containerId,
			String operatorName, int mode) {
		// Get the container id and container operator code
		String operatorCode = "";
		for (Operator operator : mOperators) {
			if (operator.getName().equals(operatorName)) {
				operatorCode = operator.getCode();
				break;
			}
		}

		switch (mode) {
		case AddContainerDialog.CONTAINER_DIALOG_ADD:
			// Create Container Session object
			User currentUser = Session.restore(getActivity()).getCurrentUser();
			ContainerSession containerSession = new ContainerSession(
					getActivity(),
					containerId,
					operatorCode,
					StringHelper
							.getCurrentTimestamp(CJayConstant.CJAY_SERVER_DATETIME_FORMAT),
					currentUser.getDepot().getDepotCode());

			containerSession.setUploadConfirmation(false);
			containerSession.setOnLocal(true);
			containerSession.setUploadState(ContainerSession.STATE_NONE);

			try {
				ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
						.getInstance().getDatabaseManager()
						.getHelper(getActivity()).getContainerSessionDaoImpl();

				containerSessionDaoImpl.addContainerSessions(containerSession);
				
				// trigger update container lists
				EventBus.getDefault().post(new ContainerCreatedEvent(containerSession));

				Intent intent = new Intent(getActivity(), CameraActivity_.class);
				intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
						containerSession.getUuid());
				intent.putExtra("type", 1); // in
				startActivity(intent);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void search(String searchText) {
		if (searchText.equals("")) {
			configureControls(mFeeds);
			mFeedsAdapter.updateData(mFeeds);
		} else {
			ArrayList<ContainerSession> searchFeeds = new ArrayList<ContainerSession>();
			for (ContainerSession containerSession : mFeeds) {
				if (containerSession.getContainerId().toLowerCase(Locale.US)
						.contains(searchText.toLowerCase(Locale.US))) {
					searchFeeds.add(containerSession);
				}
			}
			// refresh list
			configureControls(searchFeeds);
			mFeedsAdapter.updateData(searchFeeds);
		}
	}

	private void configureControls(ArrayList<ContainerSession> list) {
		boolean hasContainers = list.size() > 0;
		if (hasContainers) {
			mFeedListView.setVisibility(View.VISIBLE);
			mAddButton.setVisibility(View.INVISIBLE);
			mNotfoundTextView.setVisibility(View.INVISIBLE);
		} else {
			mFeedListView.setVisibility(View.INVISIBLE);
			mAddButton.setVisibility(View.VISIBLE);
			mNotfoundTextView.setVisibility(View.VISIBLE);
		}
	}

	private void initContainerFeedAdapter(ArrayList<ContainerSession> containers) {
		BindDictionary<ContainerSession> feedsDict = new BindDictionary<ContainerSession>();
		feedsDict.addStringField(R.id.feed_item_container_id,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_owner,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getOperatorName();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_import_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckInTime();
					}
				});
		feedsDict.addStringField(R.id.feed_item_container_export_date,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getCheckOutTime();
					}
				});
		feedsDict.addDynamicImageField(R.id.feed_item_picture,
				new StringExtractor<ContainerSession>() {
					@Override
					public String getStringValue(ContainerSession item,
							int position) {
						return item.getContainerId();
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String stringColor, ImageView view) {
						view.setImageResource(R.drawable.ic_app);
					}
				});
		mFeedsAdapter = new FunDapter<ContainerSession>(getActivity(),
				containers, R.layout.list_item_container, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
	
	public void onEvent(ContainerCreatedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerCreatedEvent");
		refresh();
	}

	public void onEvent(ContainerEditedEvent event) {
		Logger.Log(LOG_TAG, "onEvent ContainerEditedEvent");
		refresh();
	}
	
	public void refresh() {
		mFeeds = (ArrayList<ContainerSession>) DataCenter.getInstance()
				.getListLocalContainerSessions(getActivity());
		mSearchEditText.setText(""); // this will refresh the list
	}
}
