package com.cloudjay.cjay.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.SplashScreenActivity;
import com.cloudjay.cjay.dao.CJayImageDaoImpl;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.model.CJayImage;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.network.CJayClient;
import com.cloudjay.cjay.task.PhotupThreadRunnable;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.CountingInputStreamEntity;
import com.cloudjay.cjay.util.Flags;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;

public class UploadIntentService extends IntentService implements
		CountingInputStreamEntity.UploadListener {

	int increment = 10;
	int targetProgressBar = 0;
	static final int NOTIFICATION_ID = 1000;

	private NotificationManager mNotificationMgr;
	private NotificationCompat.Builder mNotificationBuilder;
	private NotificationCompat.BigPictureStyle mBigPicStyle;
	private String mNotificationSubtitle;
	private int mNumberUploaded = 0;

	private ExecutorService mExecutor;

	public UploadIntentService() {
		super("UploadIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (NetworkHelper.isConnected(getApplicationContext())) {
			try {
				cJayImageDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getCJayImageDaoImpl();
				CJayImage uploadItem = cJayImageDaoImpl.getNextWaiting();

				if (uploadItem != null) {
					doFileUpload(uploadItem);
				}

				containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getContainerSessionDaoImpl();

				// It will return container with `upload confirmation = true`
				ContainerSession containerSession = containerSessionDaoImpl
						.getNextWaiting();

				if (null != containerSession) {

					// trigger event to display in UploadsFragment

					startForeground();
					trimCache();

					// Photup implementation
					updateNotification(containerSession);

					// Self-implementation
					doUploadContainer(containerSession);

				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	void updateNotification(final ContainerSession upload) {
		String text;

		if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			final Bitmap uploadBigPic = upload.getBigPictureNotificationBmp();

			if (null == uploadBigPic) {
				mExecutor.submit(new UpdateBigPictureStyleRunnable(upload));
			}
			mBigPicStyle.bigPicture(uploadBigPic);
		}

		switch (upload.getUploadState()) {
		case ContainerSession.STATE_UPLOAD_WAITING:
			text = getString(R.string.notification_uploading_photo,
					mNumberUploaded + 1);
			mNotificationBuilder.setContentTitle(text);
			mNotificationBuilder.setTicker(text);
			mNotificationBuilder.setProgress(0, 0, true);
			mNotificationBuilder.setWhen(System.currentTimeMillis());
			break;

		case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			if (upload.getUploadProgress() >= 0) {
				text = getString(
						R.string.notification_uploading_photo_progress,
						mNumberUploaded + 1, upload.getUploadProgress());
				mNotificationBuilder.setContentTitle(text);
				mNotificationBuilder.setProgress(100,
						upload.getUploadProgress(), false);
			}
			break;
		}

		mBigPicStyle.setSummaryText(mNotificationSubtitle);
		mNotificationMgr.notify(NOTIFICATION_ID, mBigPicStyle.build());
	}

	private void trimCache() {
		CJayApplication.getApplication(this).getImageCache().trimMemory();
	}

	private void startForeground() {
		if (null == mNotificationBuilder) {
			mNotificationBuilder = new NotificationCompat.Builder(this);
			mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_upload);
			mNotificationBuilder.setContentTitle(getString(R.string.app_name));
			mNotificationBuilder.setOngoing(true);
			mNotificationBuilder.setWhen(System.currentTimeMillis());

			PendingIntent intent = PendingIntent.getActivity(this, 0,
					new Intent(this, SplashScreenActivity.class), 0);

			mNotificationBuilder.setContentIntent(intent);
		}

		if (null == mBigPicStyle) {
			mBigPicStyle = new NotificationCompat.BigPictureStyle(
					mNotificationBuilder);
		}

		startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
	}

	public void onEventMainThread(UploadStateChangedEvent event) {
		ContainerSession upload = event.getContainerSession();

		switch (upload.getUploadState()) {
		case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			updateNotification(upload);
			break;

		case ContainerSession.STATE_UPLOAD_COMPLETED:
			mNumberUploaded++;
			// Fall through...

			// case ContainerSession.STATE_UPLOAD_ERROR:
			// startNextUploadOrFinish();
			// // Fall through...

		case ContainerSession.STATE_UPLOAD_WAITING:
			if (Flags.ENABLE_DB_PERSISTENCE) {
				try {
					Logger.Log("onEventMainThread(UploadStateChangedEvent event)");
					containerSessionDaoImpl.update(upload);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	/**
	 * Upload ContainerSession to server
	 * 
	 * @param containerSession
	 */
	private void doUploadContainer(ContainerSession containerSession) {

		// post UploadStateChangedEvent
		containerSession
				.setUploadState(ContainerSession.STATE_UPLOAD_IN_PROGRESS);

		try {
			// Convert ContainerSession to TmpContainerSession for uploading
			TmpContainerSession uploadItem = Mapper.toTmpContainerSession(
					containerSession, getApplicationContext());

			// Post to Server and notify event to UploadFragment
			CJayClient.getInstance().postContainerSession(
					getApplicationContext(), uploadItem);

			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_COMPLETED);
		} catch (Exception e) {
			containerSession
					.setUploadState(ContainerSession.STATE_UPLOAD_WAITING);
		}

	}

	@Override
	public void onCreate() {

		try {
			if (null == containerSessionDaoImpl)
				containerSessionDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getContainerSessionDaoImpl();

			if (null == cJayImageDaoImpl)
				cJayImageDaoImpl = CJayClient.getInstance()
						.getDatabaseManager()
						.getHelper(getApplicationContext())
						.getCJayImageDaoImpl();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		super.onCreate();
	}

	@Override
	public void onDestroy() {

		try {
			stopForeground(true);
			finishedNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	void finishedNotification() {
		if (null != mNotificationBuilder) {
			String text = getResources().getQuantityString(
					R.plurals.notification_uploaded_photo, mNumberUploaded,
					mNumberUploaded);

			mNotificationBuilder.setOngoing(false);
			mNotificationBuilder.setProgress(0, 0, false);
			mNotificationBuilder.setWhen(System.currentTimeMillis());
			mNotificationBuilder.setContentTitle(text);
			mNotificationBuilder.setTicker(text);

			mNotificationMgr.notify(NOTIFICATION_ID,
					mNotificationBuilder.build());
		}
	}

	private CJayImageDaoImpl cJayImageDaoImpl;
	private ContainerSessionDaoImpl containerSessionDaoImpl;

	@Override
	public void onChange(int progress) {
	}

	private void doFileUpload(CJayImage uploadItem) {
		try {
			// Try New Upload Method
			uploadItem.setUploadState(CJayImage.STATE_UPLOAD_IN_PROGRESS);
			// Set Status to Uploading
			cJayImageDaoImpl.update(uploadItem);

			String uploadUrl = String.format(CJayConstant.CJAY_TMP_STORAGE,
					uploadItem.getImageName());

			final HttpResponse resp;

			// SSL Enable
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			DefaultHttpClient client = new DefaultHttpClient();

			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory
					.getSocketFactory();
			socketFactory
					.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			SingleClientConnManager mgr = new SingleClientConnManager(
					client.getParams(), registry);
			DefaultHttpClient httpClient = new DefaultHttpClient(mgr,
					client.getParams());

			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			HttpPost post = new HttpPost(uploadUrl);
			post.addHeader("Content-Type", "image/jpeg");

			ParcelFileDescriptor fileDescriptor = getContentResolver()
					.openFileDescriptor(Uri.parse(uploadItem.getUri()), "r");
			InputStream in = getContentResolver().openInputStream(
					Uri.parse(uploadItem.getUri()));
			CountingInputStreamEntity entity = new CountingInputStreamEntity(
					in, fileDescriptor.getStatSize());
			entity.setUploadListener(UploadIntentService.this);
			entity.setContentType("image/jpeg");
			post.setEntity(entity);

			try {
				Log.i("FOO", "About to call httpClient.execute");
				resp = httpClient.execute(post);
				Log.i("FOO", resp.getStatusLine().getReasonPhrase());
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
						|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
					// Set Status Success
					uploadItem.setUploadState(CJayImage.STATE_UPLOAD_COMPLETED);
					cJayImageDaoImpl.update(uploadItem);
				} else {
					Log.i("FOO", "Screw up with http - "
							+ resp.getStatusLine().getStatusCode());
				}
				resp.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			// Set Status to Uploading
			try {
				// THIS IS SQL ERROR --> NO REPEAT
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_ERROR);
				cJayImageDaoImpl.update(uploadItem);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {

			// Set Status to Uploading
			try {
				uploadItem.setUploadState(CJayImage.STATE_UPLOAD_WAITING);
				cJayImageDaoImpl.update(uploadItem);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private class UpdateBigPictureStyleRunnable extends PhotupThreadRunnable {

		private final ContainerSession mSelection;

		public UpdateBigPictureStyleRunnable(ContainerSession selection) {
			mSelection = selection;
		}

		public void runImpl() {
			mSelection.setBigPictureNotificationBmp(UploadIntentService.this,
					mSelection.getThumbnailImage(UploadIntentService.this));
			updateNotification(mSelection);
		}
	}
}
