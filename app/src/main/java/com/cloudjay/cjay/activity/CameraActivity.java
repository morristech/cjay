package com.cloudjay.cjay.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.cjayobject.AddUploadObjectCommand;
import com.cloudjay.cjay.task.command.image.AddGateImageCommand;
import com.cloudjay.cjay.task.command.image.AddOrUpdateAuditImageCommand;
import com.cloudjay.cjay.task.command.image.AddRainyImageCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.StringUtils;
import com.cloudjay.cjay.util.Utils;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

@EActivity(R.layout.activity_camera)
public class CameraActivity extends Activity implements AutoFocusCallback {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerId";
	public final static String OPERATOR_CODE_EXTRA = "com.cloudjay.wizard.operatorCode";
	public final static String IMAGE_TYPE_EXTRA = "com.cloudjay.wizard.imageType";
	public final static String CURRENT_STEP_EXTRA = "com.cloudjay.wizard.currentStep";
	// This Extra bundle is use to open Detail Issue Activity only
	public final static String AUDIT_ITEM_UUID_EXTRA = "com.cloudjay.wizard.auditItemUUID";
	public final static String IS_OPENED = "com.cloudjay.wizard.isOpened";
	public final static String OPEN_RAINY_MODE_ACTIVITY = "com.cloudjay.wizard.openRainyModeActivity";

	@Extra(IMAGE_TYPE_EXTRA)
	int mType;

	@Extra(CONTAINER_ID_EXTRA)
	String containerId;

	@Extra(OPERATOR_CODE_EXTRA)
	String operatorCode;

	@Extra(CURRENT_STEP_EXTRA)
	int currentStep;

	@Extra(AUDIT_ITEM_UUID_EXTRA)
	String auditItemUUID;

	@Extra(IS_OPENED)
	boolean isOpened;

	@Extra(OPEN_RAINY_MODE_ACTIVITY)
	boolean openRainyModeActivity;

	@ViewById(R.id.btn_camera_done)
	Button btnDone;

	@ViewById(R.id.btn_capture)
	ImageButton btnCapture;

	@ViewById(R.id.btn_toggle_flash)
	ImageButton btnToggleFlash;

	@ViewById(R.id.camera_preview)
	SurfaceView mPreview;

	@ViewById(R.id.camera_zoom_control)
	ZoomControls zoomControls;

	@ViewById(R.id.btn_use_gate_image)
	Button btnUseGateImage;

	@ViewById(R.id.tv_camera_done)
	TextView tvSavingImage;

	@SystemService
	AudioManager mAudioManager;

	@Bean
	DataCenter dataCenter;

	boolean rainyMode;
	private AuditImage auditImage;

	MediaPlayer mShootMediaPlayer = null;
	Camera mCamera = null;
	Camera.Parameters p;
	String mFlashMode = Parameters.FLASH_MODE_AUTO;

	private static final int PICTURE_SIZE_MAX_WIDTH = 640;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;
	private SurfaceHolder mPreviewHolder = null;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	int mCameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;

	int maxZoomLevel;
	int currentZoomLevel;

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			try {

				Logger.d("onSurfaceChanged");

				configurationZoom();
				initPreview(width, height);
				startPreview();

				Logger.d("endSurfaceChanged");

			} catch (Exception e) {

				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.alert_camera_preview, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
			Logger.d("onSurfaceCreated");
			mInPreview = true;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			Logger.d("onSurfaceDestroyed");

		}
	};

	private void configurationZoom() {
		//Config zoom
		p = mCamera.getParameters();
		if (p.isZoomSupported() && p.isSmoothZoomSupported()) {
			//most phones
			maxZoomLevel = p.getMaxZoom();
			final int changeValue = (maxZoomLevel / 5);
			zoomControls.setIsZoomInEnabled(true);
			zoomControls.setIsZoomOutEnabled(true);

			zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (currentZoomLevel < maxZoomLevel) {
						currentZoomLevel = currentZoomLevel + changeValue;
						mCamera.startSmoothZoom(currentZoomLevel);

					}
				}
			});

			zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (currentZoomLevel > 0) {
						currentZoomLevel = currentZoomLevel - changeValue;
						mCamera.startSmoothZoom(currentZoomLevel);
					}
				}
			});
		} else if (p.isZoomSupported() && !p.isSmoothZoomSupported()) {
			//stupid HTC phones
			maxZoomLevel = p.getMaxZoom();
			final int changeValue = (maxZoomLevel / 5);
			zoomControls.setIsZoomInEnabled(true);
			zoomControls.setIsZoomOutEnabled(true);

			zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (currentZoomLevel < maxZoomLevel) {
						currentZoomLevel = currentZoomLevel + changeValue;
						p.setZoom(currentZoomLevel);
						mCamera.setParameters(p);

					}
				}
			});

			zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (currentZoomLevel > 0) {
						currentZoomLevel = currentZoomLevel -changeValue;
						p.setZoom(currentZoomLevel);
						mCamera.setParameters(p);
					}
				}
			});
		} else {
			//no zoom on phone
			zoomControls.setVisibility(View.GONE);
		}

		mCamera.setParameters(p);
	}

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			showProgressSavingImage(true);
			try {

				savePhoto(data);
				camera.startPreview();

				mInPreview = true;
			} catch (Exception e) {

				e.printStackTrace();
				releaseCamera();

			}
		}
	};

	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {

		@Override
		public void onShutter() {
			int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

			if (volume != 0) {
				if (mShootMediaPlayer == null) {
					mShootMediaPlayer = MediaPlayer.create(getApplicationContext(),
							Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
				}

				if (mShootMediaPlayer != null) {
					mShootMediaPlayer.start();
				}
			}

			// Logger.Log( "onShutter");
		}
	};

	@Click(R.id.btn_capture)
	void buttonCaptureClicked() {
		takePicture();
	}

	@Click(R.id.btn_use_gate_image)
	void btnUseGateImageClicked() {
		// Open ReuseActivity
		Intent intent = new Intent(getApplicationContext(), ReuseActivity_.class);
		intent.putExtra(ReuseActivity_.CONTAINER_ID_EXTRA, containerId);
		startActivityForResult(intent, 1);
	}

	@Click({R.id.btn_camera_done, R.id.rl_camera_done})
	void doneButtonClicked() {
		if (rainyMode && openRainyModeActivity) {
			if (currentStep == Step.IMPORT.value) {
				// Open ReuseActivity
				Intent intent = new Intent(getApplicationContext(), RainyModeActivity_.class);
				startActivityForResult(intent, 1);
			}
		}
		finish();
	}

	private Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPictureSizes();

		return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
	}

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

	private Size determineBestPreviewSize(Camera.Parameters parameters, int w, int h) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            if (size.width <= w && size.height <= h)
            {
                if (null == result)
                    result = size;
                else
                {
                    int resultDelta = w - result.width + h - result.height;
                    int newDelta    = w - size.width   + h - size.height;

                    if (newDelta < resultDelta)
                        result = size;
                }
            }
        }
        return result;
    }

	protected Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
		Size bestSize = null;

		for (Size currentSize : sizes) {
			boolean isDesiredRatio = currentSize.width / 4 == currentSize.height / 3;
			boolean isBetterSize = bestSize == null || currentSize.width > bestSize.width;
			boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}

		return bestSize;
	}

	@AfterViews
	void afterViews() {

		// init container sessions
		// loadData();

		Logger.d("----> initCamera(), addSurfaceCallback");
		// WARNING: this block should be run before onResume()
		// Setup Surface Holder
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);
		mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Restore camera state from database or somewhere else
		// mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
		// mCameraMode = Camera.CameraInfo.CAMERA_FACING_BACK;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Enable button Camera
		btnCapture.setEnabled(true);

//		Logger.Log("isOpened: " + isOpened);

		// Config shot mode. Default is FALSE.
		// Configure View visibility based on current step of session

//		Logger.Log("Current Step of session: " + step.toString());
		rainyMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean(getString(R.string.pref_key_enable_temporary_fragment_checkbox),
						false);

		Step step = Step.values()[currentStep];
		switch (step) {
			case AUDIT:
				btnUseGateImage.setVisibility(View.VISIBLE);
				break;
			default:
				//getContract().setSingleShotMode(false);
				btnUseGateImage.setVisibility(View.GONE);
				break;
		}

	}

	void initPreview(int width, int height) {
		Logger.d("initPreview()");

		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				configurationZoom();
				Logger.d("setPreviewDisplay");

				mCamera.setPreviewDisplay(mPreviewHolder);
				// camera.setPreviewCallback(null);
				// camera.setOneShotPreviewCallback(null);

			} catch (Throwable t) {
				Logger.e("Exception in setPreviewDisplay()");
				Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!mCameraConfigured) {

				Logger.d("config Camera");

				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), width, height);
				Camera.Size pictureSize = determineBestPictureSize(parameters);

				// Logger.Log("PreviewSize: " + Integer.toString(size.width) + "/" + Integer.toString(size.height));
				// Logger.Log("PictureSize: " + Integer.toString(pictureSize.width) + "/"
				// + Integer.toString(pictureSize.height));

				try {
					if (size != null && pictureSize != null) {
						parameters.setPreviewSize(size.width, size.height);

						parameters.setPictureSize(pictureSize.width, pictureSize.height);

						parameters.setPictureFormat(ImageFormat.JPEG);
						parameters.setFlashMode(mFlashMode);
						parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

						mCamera.setParameters(parameters);
						mCameraConfigured = true;
					}
				} catch (Resources.NotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {

		try {
			Logger.d("Auto focused, now take picture");
			mCamera.takePicture(shutterCallback, null, photoCallback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		doneButtonClicked();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// cameraConfigured = false;
		super.onConfigurationChanged(newConfig);

		// setCameraDisplayOrientation(this, cameraMode, camera);
		// onResume();

		setContentView(R.layout.activity_camera);
		openCamera();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		try {
			if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
					|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
					|| keyCode == KeyEvent.KEYCODE_CAMERA)) {

				takePicture();
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} catch (Exception e) {
			Utils.showCrouton(this, "Please take it easy");
			e.printStackTrace();
			return true;
		}
	}

	@Override
	protected void onPause() {
		releaseCamera();
		super.onPause();
	}

	// region Override Activity
	@Override
	protected void onResume() {

		// Logger.w("Session: " + mContainerSessionUUID);
		// Logger.w("Issue: " + mIssueUUID);
		// Logger.w("Image Type: " + mType);
		// Logger.Log("----> onResume()");
		super.onResume();

		openCamera();
		setContentView(R.layout.activity_camera);

	}

	void openCamera() {

		if (mCamera != null) {
			mCamera.release();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();

			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

				Camera.getCameraInfo(i, info);
				if (info.facing == mCameraMode) {
					Logger.d("inside onResume: camera != null");

					mCamera = Camera.open(i);
					setCameraDisplayOrientation(this, mCameraMode, mCamera);
				}
			}
		}

		if (mCamera == null) {
			Logger.d("inside onResume: camera == null");
			mCamera = Camera.open(mCameraMode);
		}

		startPreview();
	}

	void releaseCamera() {

		if (mCamera != null) {

			Logger.d("Release camera ... ");

			if (mInPreview) {
				Logger.d("Stop Preview ... ");
				mCamera.stopPreview();
				// preview.getHolder().removeCallback(null);
			}

			// camera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;

			// WARNING: lots of bugs may appear
			mInPreview = false;
			mCameraConfigured = false;

			Logger.d("Release camera complete");
		}
	}

	void saveBitmapToFile(Bitmap bitmap, File filename) {

		Logger.d("===== On SaveBitmap =====");
		Logger.d("Width/Height: " + Integer.toString(bitmap.getWidth()) + "/" +
				Integer.toString(bitmap.getHeight()));

		try {

			// Logger.Log("Path: " + filename.getAbsolutePath());

			FileOutputStream out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Background
	void savePhoto(byte[] data) {

		try {

			// Convert rotated byte[] to Bitmap
			Bitmap capturedBitmap = saveToBitmap(data);

			//Random UUID
			String uuid = UUID.randomUUID().toString();

			// Save bitmap
			File photo = getFile(uuid);
			saveBitmapToFile(capturedBitmap, photo);

			// Add taken picture to job queue
			addImageToUploadQueue(photo.getAbsolutePath(), photo.getName(), uuid);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showProgressSavingImage(false);
				}
			});

		} catch (Exception e) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Không thể lưu hình, vui lòng thử lại", Toast.LENGTH_SHORT).show();
					showProgressSavingImage(false);
				}
			});
		}
	}

	/**
	 * save byte array to Bitmap
	 *
	 * @param data
	 * @return
	 */
	Bitmap saveToBitmap(byte[] data) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		if (data != null) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);

				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(mCameraMode, info);

				int rotation = getWindowManager().getDefaultDisplay().getRotation();

				Matrix mtx = new Matrix();

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && rotation == Surface.ROTATION_0) {
					mtx.postRotate(270);
				} else {
					mtx.postRotate(90);
				}

				Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);

				if (bm != null) {
					bm.recycle();
					bm = null;
					System.gc();
				}

				return rotatedBitmap;

			} else {

				int rotation = getWindowManager().getDefaultDisplay().getRotation();

				if (rotation == Surface.ROTATION_270) {

					Matrix mtx = new Matrix();
					mtx.postRotate(180);

					// Flip Bitmap
					Logger.Log("Flip image");
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);
					bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
					return bm;

				} else {
					// LANDSCAPE MODE --> No need to reverse width and height
					Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data != null ? data.length : 0);
					return bm;
				}
			}
		}
		return null;
	}

	void startPreview() {
		Logger.d("----> startPreview");

		if (mCameraConfigured && mCamera != null) {

			Logger.d("cameraConfigured and camera != null");
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	@Background
	void takePicture() {

		if (mInPreview) {

			try {
				// Logger.Log( "Prepare to take picture");
				Camera.Parameters cameraParameters = mCamera.getParameters();

				List<String> supportedFocusMode = cameraParameters.getSupportedFocusModes();

				// Submit focus area to camera
				if (supportedFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {

					cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					mCamera.setParameters(cameraParameters);
					mCamera.autoFocus(this);

				} else {

					Logger.Log("No auto focus mode supported, now just take picture");
					mCamera.takePicture(shutterCallback, null, photoCallback);

				}

				mInPreview = false;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Default:
	 * <p/>
	 * OFF --> AUTO --> ON --> OFF
	 */
	@Click(R.id.btn_toggle_flash)
	void toggleFlashButtonClicked() {

		// Logger.Log( "toggleFlashButtonClicked()");

		if (mInPreview && mCamera != null) {
			Parameters params = mCamera.getParameters();
			mFlashMode = params.getFlashMode();

			if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_OFF)) {

				btnToggleFlash.setImageResource(R.drawable.ic_flash_auto);
				params.setFlashMode(Parameters.FLASH_MODE_AUTO);

			} else if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_AUTO)) {

				btnToggleFlash.setImageResource(R.drawable.ic_flash_on);
				params.setFlashMode(Parameters.FLASH_MODE_ON);

			} else if (mFlashMode.equalsIgnoreCase(Parameters.FLASH_MODE_ON)) {

				btnToggleFlash.setImageResource(R.drawable.ic_flash_off);
				params.setFlashMode(Parameters.FLASH_MODE_OFF);

			} else {

			}

			// Update camera and camera setting
			mCamera.setParameters(params);
			mFlashMode = params.getFlashMode();

		} else {
			Logger.Log("Camera does not open");
		}
	}

	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {

		if (camera != null) {

			android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(cameraId, info);

			int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

			int degrees = 0;
			switch (rotation) {
				case Surface.ROTATION_0:
					degrees = 0;
					break;
				case Surface.ROTATION_90:
					degrees = 90;
					break;
				case Surface.ROTATION_180:
					degrees = 180;
					break;
				case Surface.ROTATION_270:
					degrees = 270;
					break;
			}

			Logger.Log("Rotate degree: " + degrees);

			int result;
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360; // compensate the mirror

			} else { // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}

			camera.setDisplayOrientation(result);
		}
	}

	/**
	 * Add new image to database based on `mType`.
	 * Add image at `uri` to job queue.
	 *
	 * @param uri
	 * @param imageName
	 * @throws com.snappydb.SnappydbException
	 */
	protected void addImageToUploadQueue(String uri, String imageName, String uuid) {

		// Create image based on mType and add this image to database
		ImageType type = ImageType.values()[mType];
		switch (type) {
			case IMPORT:
				if (rainyMode && openRainyModeActivity) {
					uri = "file://" + uri;
					dataCenter.add(new AddRainyImageCommand(getApplicationContext(),
							uuid, uri));
					return;
				}
			case EXPORT:
				GateImage gateImage = new GateImage()
						.withId(0)
						.withType(mType)
						.withName(imageName)
						.withUrl("file://" + uri)
						.withUuid(uuid);

				dataCenter.add(new AddGateImageCommand(getApplicationContext(), gateImage, containerId));

				// Add image to job queue
				UploadObject object = new UploadObject(gateImage, GateImage.class, containerId);
				dataCenter.add(new AddUploadObjectCommand(getApplicationContext(), object));

				break;

			case AUDIT:
			case REPAIRED:
			default:

				Logger.Log("mType: " + mType);
				auditImage = new AuditImage()
						.withId(0)
						.withType(mType)
						.withUrl("file://" + uri)
						.withName(imageName)
						.withUUID(uuid);

				dataCenter.add(new AddOrUpdateAuditImageCommand(getApplicationContext(), auditImage, containerId, auditItemUUID));
				// Add image to job queue
				UploadObject objectAudit = new UploadObject(auditImage, AuditImage.class, containerId);
				dataCenter.add(new AddUploadObjectCommand(getApplicationContext(), objectAudit));
				break;
		}
	}

	/**
	 * Create directory for saving image
	 *
	 * @return
	 */
	protected File getFile(String uuid) {

		String fileName;
		File newDirectory = null;

		// create today String
		String today = StringUtils.getCurrentTimestamp(CJayConstant.DAY_FORMAT);

		// get depot code
		String depotCode = PreferencesUtil.getPrefsValue(getApplicationContext(), PreferencesUtil.PREF_USER_DEPOT);

		if (rainyMode && openRainyModeActivity) {

			// create directory to save images
			newDirectory = new File(CJayConstant.APP_DIRECTORY_FILE, depotCode + "/rainy_mode");

			if (!newDirectory.exists()) {
				newDirectory.mkdirs();
			}

			// create image file name
			fileName = depotCode + "-" + today + "-" + "imageType" + "-" + "containerId" + "-"
					+ uuid + ".jpg";
		} else {
			// Save Bitmap to Files
			String imageType = getImageTypeString(mType);

			// create directory to save images
			newDirectory = new File(CJayConstant.APP_DIRECTORY_FILE, depotCode + "/" + today + "/" + imageType
					+ "/" + containerId);

			if (!newDirectory.exists()) {
				newDirectory.mkdirs();
			}

			// create image file name
			fileName = depotCode + "-" + today + "-" + imageType + "-" + containerId + "-"
					+ uuid + ".jpg";
		}

		File photo = new File(newDirectory, fileName);

		return photo;
	}

	private void showProgressSavingImage(boolean show) {
		btnCapture.setEnabled(show ? false : true);
		btnDone.setEnabled(show ? false : true);
		btnDone.setVisibility(show ? View.GONE : View.VISIBLE);
		tvSavingImage.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	/**
	 * Get image type string based on input image type
	 *
	 * @param imgType
	 * @return
	 */
	protected String getImageTypeString(int imgType) {
		ImageType type = ImageType.values()[imgType];
		switch (type) {
			case IMPORT:
				return "gate-in";

			case EXPORT:
				return "gate-out";

			case AUDIT:
				return "auditor";

			case REPAIRED:
			default:
				return "repair";
		}
	}
}
