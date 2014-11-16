package com.cloudjay.cjay.task.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.path.android.jobqueue.JobManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;

import retrofit.RetrofitError;

@EIntentService
public class UploadIntentService extends IntentService {

	@Bean
	DataCenter dataCenter;

	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {

			// Check if user is logged in or not
			String token = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_TOKEN);
			if (!TextUtils.isEmpty(token) && Utils.canReachInternet()) {

				JobManager manager = App.getJobManager();
				if (manager.count() != 0) {
					Logger.Log("There is already job in the queue");
				} else {
					dataCenter.startJobQueue(getApplicationContext());
				}

			} else {
				Logger.w("There was problems. Please check credential or connectivity.");
			}

		} catch (RetrofitError e) {
			e.printStackTrace();
			Utils.cancelAlarm(getApplicationContext());
		}
	}
}