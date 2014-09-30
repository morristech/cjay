package com.cloudjay.cjay.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.TabHostPagerAdapter;
import com.cloudjay.cjay.fragment.SearchFragment;
import com.cloudjay.cjay.fragment.UploadFragment;
import com.cloudjay.cjay.fragment.WorkingFragment;

import java.util.List;
import java.util.Vector;

import butterknife.InjectView;

/**
 * Created by Thai on 9/30/2014.
 */
public class HomeActivity extends BaseActivity {

	@InjectView(R.id.tabhost)
	FragmentTabHost mTabhost;

	@InjectView(R.id.viewpager)
	ViewPager mViewPager;

	PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		initTabHost();
		initViewPager();

	}

	private void initViewPager() {
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		List<android.support.v4.app.Fragment> fragments = new Vector<android.support.v4.app.Fragment>();
		fragments.add(android.support.v4.app.Fragment.instantiate(this, SearchFragment.class.getName()));
		fragments.add(android.support.v4.app.Fragment.instantiate(this, WorkingFragment.class.getName()));
		fragments.add(android.support.v4.app.Fragment.instantiate(this, UploadFragment.class.getName()));
		mPagerAdapter = new TabHostPagerAdapter(getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mTabhost.setCurrentTab(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


	}

	private void initTabHost() {
		mTabhost = (FragmentTabHost) findViewById(R.id.tabhost);
		mTabhost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		mTabhost.addTab(mTabhost.newTabSpec("tab1").setIndicator("Tìm kiếm"), SearchFragment.class, null);
		mTabhost.addTab(mTabhost.newTabSpec("tab1").setIndicator("Đang làm"), WorkingFragment.class, null);
		mTabhost.addTab(mTabhost.newTabSpec("tab1").setIndicator("Tải lên"), UploadFragment.class, null);
	}
}
