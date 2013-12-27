package com.cloudjay.cjay;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.cloudjay.cjay.adapter.ViewPagerAdapter;
import com.cloudjay.cjay.fragment.GateImportListFragment;
import com.cloudjay.cjay.listener.OnContainerAddRequestListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_gate_home)
public class GateHomeActivity extends CJayActivity implements
		OnPageChangeListener, TabListener, OnContainerAddRequestListener {

	private String[] locations;
	private ViewPagerAdapter mPagerAdapter;
	
	@ViewById
	ViewPager pager;

	@AfterViews
	void afterViews() {
		locations = getResources().getStringArray(R.array.gate_home_tabs);
		configureViewPager();
		configureActionBar();
	}

	private void configureViewPager() {
		mPagerAdapter = new ViewPagerAdapter(
				getSupportFragmentManager(), locations);
		pager.setAdapter(mPagerAdapter);
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
	public void OnContainerAddRequested() {
		// go to import tab
		getSupportActionBar().selectTab(getSupportActionBar().getTabAt(0));
		
		// show add container dialog
		GateImportListFragment importListFragment = (GateImportListFragment) mPagerAdapter.instantiateItem(pager, 0);
		importListFragment.handleAddContainer();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
