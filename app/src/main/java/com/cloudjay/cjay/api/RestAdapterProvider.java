package com.cloudjay.cjay.api;

import android.content.Context;

import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Util;

import org.androidannotations.annotations.EBean;

import retrofit.RestAdapter;


@EBean
public class RestAdapterProvider {

	public RestAdapter getRestAdapter(Context context) {

		// Get current user token
		User user = PreferencesUtil.getObject(context, PreferencesUtil.PREF_CURRENT_USER, User.class);
		String username = "";
		String token;

		if (user == null) {
			token = PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_TOKEN);
		} else {
			username = user.getUsername();
			token = user.getToken();
		}

		// Init header, pass 3 params: token, app version, username
		String appVersion = Util.getAppVersionName(context);
		ApiHeaders headers = new ApiHeaders(token, appVersion, username);
		return new RestAdapter.Builder().setEndpoint(ApiEndpoint.ROOT_API).setRequestInterceptor(headers).build();
	}
}