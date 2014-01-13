package com.cloudjay.cjay.receivers;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (NetworkHelper.isConnected(context)) {

			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, false);

			if (!Utils.isAlarmUp(context)) {
				Utils.startAlarm(context);
			}

			Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();

		} else {

			// Mark that device has no data connection
			PreferencesUtil.storePrefsValue(context,
					PreferencesUtil.PREF_NO_CONNECTION, true);

			if (Utils.isAlarmUp(context)) {
				Utils.cancelAlarm(context);
			}

			// EventBus.getDefault().post(new NoConnectionEvent());
			Toast.makeText(context, "Not connect to Internet",
					Toast.LENGTH_SHORT).show();
		}
	}
}
