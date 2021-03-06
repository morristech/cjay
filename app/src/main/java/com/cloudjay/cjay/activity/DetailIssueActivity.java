package com.cloudjay.cjay.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.cloudjay.cjay.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by thai on 27/10/2014.
 */
@EActivity(R.layout.activity_detail_issue)
public class DetailIssueActivity extends BaseActivity implements ActionBar.TabListener {

    public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";

    public final static String AUDIT_ITEM_EXTRA = "com.cloudjay.wizard.auditItem";
	public final static String SELECTED_TAB = "com.cloudjay.wizzard.selectedTab";

    @Extra(CONTAINER_ID_EXTRA)
    public String containerID;

    @Extra(AUDIT_ITEM_EXTRA)
    String auditItemUUID;

	@Extra(SELECTED_TAB)
	int selectedTab;

    @ViewById(R.id.pager_repaired)
    ViewPager pager;

    ActionBar actionBar;
    private com.cloudjay.cjay.adapter.ViewPagerAdapter mPagerAdapter;
    public int currentPosition = 0;

    @AfterViews
    void doAfterViews() {

        configureActionBar();
        configureViewPager();
    }

    private void configureViewPager() {
        mPagerAdapter = new com.cloudjay.cjay.adapter.ViewPagerAdapter(this, this.getSupportFragmentManager(), containerID, auditItemUUID, 2);
        pager.setAdapter(mPagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                ActionBar.Tab tab = actionBar.getTabAt(position);
                actionBar.selectTab(tab);
            }

        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
			if (i == selectedTab) {
				actionBar.addTab(
						actionBar.newTab()
								.setText(mPagerAdapter.getPageTitle(i))
								.setTabListener(this), true);
			} else {
				actionBar.addTab(
						actionBar.newTab()
								.setText(mPagerAdapter.getPageTitle(i))
								.setTabListener(this), false);
			}
        }

    }

    private void configureActionBar() {
        // Get actionbar
        actionBar = getActionBar();

        // Set ActionBar Title
        actionBar.setTitle(R.string.fragment_repair_title);

        // Fix tab layo
        final Method method;
        try {
            method = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", new Class[]{Boolean.TYPE});
            method.setAccessible(true);
            method.invoke(actionBar, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set Providing Up Navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        pager.setCurrentItem(position);
        currentPosition = position;

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
