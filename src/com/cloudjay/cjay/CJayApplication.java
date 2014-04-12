package com.cloudjay.cjay;

import java.sql.SQLException;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.androidannotations.annotations.EApplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.ContainerSessionEnqueueEvent;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.network.HttpRequestWrapper;
import com.cloudjay.cjay.network.IHttpRequestWrapper;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CJaySession;
import com.cloudjay.cjay.util.DataCenter;
import com.cloudjay.cjay.util.DatabaseManager;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import de.greenrobot.event.EventBus;

@ReportsCrashes(formKey = "",
				formUri = CJayConstant.ACRA,
				mode = ReportingInteractionMode.TOAST,
				resToastText = R.string.crash_toast_text,
				resDialogText = R.string.crash_dialog_text,
				resDialogIcon = android.R.drawable.ic_dialog_info,
				resDialogTitle = R.string.crash_dialog_title,
				resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
				resDialogOkToast = R.string.crash_dialog_ok_toast)
@EApplication
public class CJayApplication extends Application {

	public static void gotoCamera(Context ctx, ContainerSession containerSession, int imageType, String activityTag) {

		Intent intent = new Intent(ctx, CameraActivity_.class);
		intent.putExtra(CameraActivity.CJAY_CONTAINER_SESSION_EXTRA, containerSession.getUuid());
		intent.putExtra(CameraActivity.CJAY_IMAGE_TYPE_EXTRA, imageType);

		if (activityTag != null && !TextUtils.isEmpty(activityTag)) {
			intent.putExtra(CameraActivity.SOURCE_TAG_EXTRA, activityTag);
		}

		ctx.startActivity(intent);
	}

	public static void logOutInstantly(Context ctx) {

		Logger.w("Access Token is expired");
		CJaySession session = CJaySession.restore(ctx);
		session.deleteSession(ctx);

		Intent intent = new Intent(ctx, LoginActivity_.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);

		// http://stackoverflow.com/questions/3007998/on-logout-clear-activity-history-stack-preventing-back-button-from-opening-l
		// Intent intent = new Intent();
		// broadcastIntent.setAction("com.package.ACTION_LOGOUT");
		// sendBroadcast(broadcastIntent);
	}

	public static void openPhotoGridView(Context ctx, String uuid, String containerId, int imageType1, int imageType2,
											String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);

		intent.putExtra(PhotoExpandableListViewActivity.CJAY_CONTAINER_SESSION_UUID_EXTRA, uuid);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_IMAGE_TYPE_1_EXTRA, imageType1);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_IMAGE_TYPE_2_EXTRA, imageType2);
		intent.putExtra("tag", sourceTag);

		Logger.w("Open Photo Grid View with imageType: " + Integer.toString(imageType1) + " | "
				+ Integer.toString(imageType2));

		ctx.startActivity(intent);
	}

	public static void openPhotoGridView(Context ctx, String uuid, String containerId, int imageType, String sourceTag) {

		Intent intent = new Intent(ctx, PhotoExpandableListViewActivity_.class);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_CONTAINER_SESSION_UUID_EXTRA, uuid);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_IMAGE_TYPE_1_EXTRA, imageType);
		intent.putExtra(PhotoExpandableListViewActivity.CJAY_CONTAINER_ID_EXTRA, containerId);
		intent.putExtra("tag", sourceTag);

		ctx.startActivity(intent);

	}

	public static void startCJayHomeActivity(Context context) {

		/**
		 * Gate: 6 | Audit: 1 | Repair: 4
		 */

		Logger.Log("start CJayHome Activity");
		int userRole = ((CJayActivity) context).getCurrentUser().getRole();

		Intent intent = null;
		switch (userRole) {
			case 1:
				intent = new Intent(context, AuditorHomeActivity_.class);
				break;

			case 4:
				intent = new Intent(context, RepairHomeActivity_.class);
				break;

			case 6:
			default:
				intent = new Intent(context, GateHomeActivity_.class);
				break;
		}
		context.startActivity(intent);
	}

	public static void uploadContainerSesison(Context ctx, ContainerSession containerSession) {

		ContainerSessionDaoImpl containerSessionDaoImpl = null;

		// User confirm upload
		containerSession.setUploadConfirmation(true);
		containerSession.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);

		if (null == containerSessionDaoImpl) {
			try {
				containerSessionDaoImpl = CJayClient.getInstance().getDatabaseManager().getHelper(ctx)
													.getContainerSessionDaoImpl();

				containerSessionDaoImpl.update(containerSession);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// It will trigger `UploadsFragment` Adapter
		EventBus.getDefault().post(new ContainerSessionEnqueueEvent(containerSession));

	}

	IDatabaseManager databaseManager = null;

	IHttpRequestWrapper httpRequestWrapper = null;

	static Context mContext = null;

	public static Context getContext() {
		return mContext;
	}

	@Override
	public void onCreate() {
		Logger.Log("Start Application");

		// Configure Logger
		Logger.getInstance().setDebuggable(true);

		// Ion.getDefault(getBaseContext()).configure()
		// .setLogging("Network Module", Log.INFO);

		super.onCreate();
		databaseManager = new DatabaseManager();
		httpRequestWrapper = new HttpRequestWrapper();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
																				.cacheOnDisc(true)
																				.bitmapConfig(Bitmap.Config.RGB_565)
																				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
																				.showImageForEmptyUri(R.drawable.ic_app)
																				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(	defaultOptions)
																										.discCacheSize(	100 * 1024 * 1024)
																										.memoryCache(	new WeakMemoryCache())
																										.threadPoolSize(3)
																										.threadPriority(Thread.MAX_PRIORITY)
																										.build();

		ACRA.init(CJayApplication.this);
		ACRA.getErrorReporter().checkReportsOnApplicationStart();

		ImageLoader.getInstance().init(config);
		CJayClient.getInstance().init(httpRequestWrapper, databaseManager);
		DataCenter.getInstance().initialize(databaseManager);
		databaseManager.getHelper(getApplicationContext());

		if (!CJayConstant.APP_DIRECTORY_FILE.exists()) {
			CJayConstant.APP_DIRECTORY_FILE.mkdir();
		}

		if (!CJayConstant.HIDDEN_APP_DIRECTORY_FILE.exists()) {
			CJayConstant.HIDDEN_APP_DIRECTORY_FILE.mkdir();
		}

		// Configure Alarm Manager
		mContext = getApplicationContext();
		if (!Utils.isAlarmUp(mContext)) {

			Logger.w("Alarm Manager is not running.");
			Utils.startAlarm(mContext);

		}

		if (NetworkHelper.isConnected(this)) {
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_NO_CONNECTION, false);
		} else {
			PreferencesUtil.storePrefsValue(this, PreferencesUtil.PREF_NO_CONNECTION, true);
		}
	}
}
