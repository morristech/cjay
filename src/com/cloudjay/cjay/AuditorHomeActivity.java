package com.cloudjay.cjay;

import java.lang.reflect.Field;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.fragment.*;
import com.cloudjay.cjay.view.AddContainerDialog;
import com.cloudjay.cjay.view.SearchOperatorDialog;

@EActivity(R.layout.activity_auditor_home)
public class AuditorHomeActivity extends CJayActivity implements
		OnPageChangeListener, TabListener,
		AddContainerDialog.AddContainerDialogListener,
		SearchOperatorDialog.SearchOperatorDialogListener {

	private ViewPagerAdapter viewPagerAdapter;
	private String[] locations;

	@ViewById
	ViewPager pager;

	@Override
	protected void onCreate(Bundle arg0) {

		// Below code to show `More Action` item on menu
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {

		}
		super.onCreate(arg0);
	}

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(R.array.auditor_home_tabs);
		configureViewPager();
		configureActionBar();
	}

	private void configureViewPager() {
		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
				locations) {

			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 0:
					// containers that have no 'report images'
					Fragment notReportedFragment = new AuditorReportingListFragment_();
					((AuditorReportingListFragment_) notReportedFragment)
							.setState(AuditorReportingListFragment_.STATE_NOT_REPORTED);
					return notReportedFragment;

				case 1:
					// containers that have 'report images'
					Fragment reportingFragment = new AuditorReportingListFragment_();
					((AuditorReportingListFragment_) reportingFragment)
							.setState(AuditorReportingListFragment_.STATE_REPORTING);
					return reportingFragment;

				case 2:
				default:
					Fragment uploadFragment = new UploadsFragment_();
					return uploadFragment;
				}
			}
		};
		pager.setAdapter(viewPagerAdapter);
		pager.setOnPageChangeListener(this);
	}

	public void onPageSelected(int position) {
		Tab tab = getSupportActionBar().getTabAt(position);
		getSupportActionBar().selectTab(tab);
	}

	private void configureActionBar() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (String location : locations) {
			Tab tab = getSupportActionBar().newTab();
			tab.setText(location);
			tab.setTabListener(this);
			getSupportActionBar().addTab(tab);
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int position = tab.getPosition();
		pager.setCurrentItem(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater();
		return true;
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void OnOperatorSelected(Fragment parent, String containerId,
			String operatorName, int mode) {
		if (parent instanceof AuditorReportingListFragment) {
			((AuditorReportingListFragment) parent).OnOperatorSelected(
					containerId, operatorName, mode);
		}
	}

	@Override
	public void OnContainerInputCompleted(Fragment parent, String containerId,
			String operatorName, int mode) {
		if (parent instanceof AuditorReportingListFragment) {
			((AuditorReportingListFragment) parent).OnContainerInputCompleted(
					containerId, operatorName, mode);
		}
	}

}
