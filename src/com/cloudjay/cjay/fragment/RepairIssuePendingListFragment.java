package com.cloudjay.cjay.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.ami.fundapter.interfaces.DynamicImageLoader;
import com.cloudjay.cjay.*;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.IssueDaoImpl;
import com.cloudjay.cjay.events.CJayImageAddedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Issue;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.util.Utils;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_repair_issue_pending)
public class RepairIssuePendingListFragment extends SherlockFragment {

	private final String LOG_TAG = "RepairPendingIssueListFragment";
	
	private ArrayList<Issue> mFeeds;
	private FunDapter<Issue> mFeedsAdapter;
	private ContainerSession mContainerSession;
	private String mContainerSessionUUID;
	private Issue mSelectedIssue;
	private ImageLoader imageLoader;
	private ArrayList<CJayImage> mTakenImages;

	@ViewById(R.id.feeds)
	ListView mFeedListView;

	@AfterViews
	void afterViews() {
		try {
			imageLoader = ImageLoader.getInstance();

			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(getActivity())
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);

			if (null != mContainerSession) {
				populateIssueList();
				initIssueFeedAdapter(mFeeds);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mSelectedIssue = null;
	}

	@ItemClick(R.id.feeds)
	void imageItemClicked(int position) {
		mSelectedIssue = mFeedsAdapter.getItem(position);
		
		// show issue report activity
		Intent intent = new Intent(getActivity(), RepairIssueReportActivity_.class);
		intent.putExtra(RepairIssueReportActivity_.CJAY_ISSUE_EXTRA,
				mSelectedIssue.getUUID());
		startActivity(intent);
	}

	@Click(R.id.btn_add_new)
	void cameraClicked() {
		mTakenImages = new ArrayList<CJayImage>();
		
		Intent intent = new Intent(getActivity(), CameraActivity_.class);
		intent.putExtra(CameraActivity_.CJAY_CONTAINER_SESSION_EXTRA,
				mContainerSession.getUuid());
		intent.putExtra("type", CJayImage.TYPE_REPORT);
		intent.putExtra("tag", LOG_TAG);
		startActivity(intent);
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
	
	@Override
	public void onResume() {
		super.onResume();
		
		// create issue if needed
		if (mTakenImages != null && mTakenImages.size() > 0) {
			try {
				IssueDaoImpl issueDaoImpl = CJayClient.getInstance()
						.getDatabaseManager().getHelper(getActivity()).getIssueDaoImpl();
				CJayImageDaoImpl cJayImageDaoImpl = CJayClient.getInstance()
						.getDatabaseManager().getHelper(getActivity()).getCJayImageDaoImpl();
				
				// create and save issue
				Issue issue = new Issue();
				issue.setContainerSession(mContainerSession);
				
				for (CJayImage cJayImage : mTakenImages) {
					cJayImage.setIssue(issue);
					cJayImage.setContainerSession(mContainerSession);
					cJayImageDaoImpl.createOrUpdate(cJayImage);
				}
				issueDaoImpl.createOrUpdate(issue);
				
				// show view to edit issue
				Intent intent = new Intent(getActivity(), AuditorIssueReportActivity_.class);
				intent.putExtra(AuditorIssueReportActivity_.CJAY_IMAGE_EXTRA, mTakenImages.get(0).getUuid());
				startActivity(intent);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			mTakenImages.clear();
			mTakenImages = null;
			
		} else {
			populateIssueList();
			mFeedsAdapter.updateData(mFeeds);
		}
	}
	
	public void setContainerSessionUUID(String containerSessionUUID) {
		mContainerSessionUUID = containerSessionUUID;
	}

	private void populateIssueList() {
		mFeeds = new ArrayList<Issue>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = CJayClient
					.getInstance().getDatabaseManager().getHelper(getActivity())
					.getContainerSessionDaoImpl();
			mContainerSession = containerSessionDaoImpl
					.queryForId(mContainerSessionUUID);
			
			if (null != mContainerSession) {
				for (Issue issue : mContainerSession.getIssues()) {
					if (!issue.isFixed()) {
						mFeeds.add(issue);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void onEvent(CJayImageAddedEvent event) {
		// retrieve image
		if (event.getTag().equals(LOG_TAG)) {
			mTakenImages.add(event.getCJayImage());
		}
	}

	private void initIssueFeedAdapter(ArrayList<Issue> containers) {
		BindDictionary<Issue> feedsDict = new BindDictionary<Issue>();
		feedsDict.addStringField(R.id.issue_location_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLocationCode());
					}
				});
		feedsDict.addStringField(R.id.issue_damage_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getDamageCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_repair_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getRepairCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_component_code,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getComponentCodeString());
					}
				});
		feedsDict.addStringField(R.id.issue_quantity,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getQuantity());
					}
				});
		feedsDict.addStringField(R.id.issue_length,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getLength());
					}
				});
		feedsDict.addStringField(R.id.issue_height,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						return Utils.stripNull(item.getHeight());
					}
				});
		feedsDict.addDynamicImageField(R.id.issue_picture,
				new StringExtractor<Issue>() {
					@Override
					public String getStringValue(Issue item, int position) {
						for (CJayImage cJayImage : item.getCJayImages()) {
							if (!TextUtils.isEmpty(cJayImage.getUri())) {
								return cJayImage.getUri();
							}
						}
						return null;
					}
				}, new DynamicImageLoader() {
					@Override
					public void loadImage(String url, ImageView view) {
						if (url != null && !TextUtils.isEmpty(url)) {
							imageLoader.displayImage(url, view);
						}
					}
				});
		mFeedsAdapter = new FunDapter<Issue>(getActivity(), containers, R.layout.list_item_issue, feedsDict);
		mFeedListView.setAdapter(mFeedsAdapter);
	}
}
