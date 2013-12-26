/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudjay.cjay.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.model.GateReportImage;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.task.PhotupThreadRunnable;
import com.cloudjay.cjay.util.Flags;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapWrapper;
import uk.co.senab.bitmapcache.CacheableImageView;

public class PhotupImageView extends CacheableImageView {

	public static interface OnPhotoLoadListener {

		void onPhotoLoadFinished(Bitmap bitmap);
	}

	static final class PhotoLoadRunnable extends PhotupThreadRunnable {

		private final WeakReference<PhotupImageView> mImageView;
		private final GateReportImage mUpload;
		private final boolean mFullSize;
		private final BitmapLruCache mCache;
		private final OnPhotoLoadListener mListener;

		public PhotoLoadRunnable(PhotupImageView imageView,
				GateReportImage upload, BitmapLruCache cache,
				final boolean fullSize, final OnPhotoLoadListener listener) {
			mImageView = new WeakReference<PhotupImageView>(imageView);
			mUpload = upload;
			mFullSize = fullSize;
			mCache = cache;
			mListener = listener;
		}

		public void runImpl() {
			final PhotupImageView imageView = mImageView.get();
			if (null == imageView) {
				return;
			}

			// final Context context = imageView.getContext();
			// final CacheableBitmapWrapper wrapper;
			//
			// final Bitmap bitmap = mFullSize ?
			// mUpload.getDisplayImage(context)
			// : mUpload.getThumbnailImage(context);
			//
			// if (null != bitmap) {
			// final String key = mFullSize ? mUpload.getDisplayImageKey()
			// : mUpload.getThumbnailImageKey();
			// wrapper = new CacheableBitmapWrapper(key, bitmap);
			// } else {
			// wrapper = null;
			// }
			//
			// // If we're interrupted, just update the cache and return
			// if (isInterrupted()) {
			// mCache.put(wrapper);
			// return;
			// }
			//
			// // If we're still running, update the Views
			// if (null != wrapper) {
			// imageView.post(new Runnable() {
			// public void run() {
			// imageView.setImageCachedBitmap(wrapper);
			// mCache.put(wrapper);
			//
			// if (null != mListener) {
			// mListener.onPhotoLoadFinished(wrapper.getBitmap());
			// }
			// }
			// });
			// }
		}
	}

	private boolean mFadeInDrawables = false;
	private Drawable mFadeFromDrawable;
	private int mFadeDuration;

	private Future<?> mCurrentRunnable;

	public PhotupImageView(Context context) {
		super(context);
	}

	public PhotupImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void cancelRequest() {
		if (null != mCurrentRunnable) {
			mCurrentRunnable.cancel(true);
			mCurrentRunnable = null;
		}
	}

	public Bitmap getCurrentBitmap() {
		Drawable d = getDrawable();
		if (d instanceof BitmapDrawable) {
			return ((BitmapDrawable) d).getBitmap();
		}

		return null;
	}

	public void recycleBitmap() {
		Bitmap currentBitmap = getCurrentBitmap();
		if (null != currentBitmap) {
			setImageDrawable(null);
			currentBitmap.recycle();
		}
	}

	public void requestFullSize(final TmpContainerSession upload,
			final boolean honourFilter, final boolean clearDrawableOnLoad,
			final OnPhotoLoadListener listener) {
		resetForRequest(clearDrawableOnLoad);

		// Show thumbnail if it's in the cache
		// BitmapLruCache cache = CJayApplication.getApplication(
		// getContext()).getImageCache();
		// CacheableBitmapWrapper thumbWrapper = cache.get(upload
		// .getThumbnailImageKey());
		// if (null != thumbWrapper && thumbWrapper.hasValidBitmap()) {
		// if (Flags.DEBUG) {
		// Log.d("requestFullSize", "Got Cached Thumbnail");
		// }
		// setImageCachedBitmap(thumbWrapper);
		// } else {
		// setImageDrawable(null);
		// }
		//
		// requestImage(upload, true, listener);
	}

	public void requestFullSize(final TmpContainerSession upload,
			final boolean honourFilter, final OnPhotoLoadListener listener) {
		requestFullSize(upload, honourFilter, true, listener);
	}

	public void requestThumbnail(final GateReportImage upload,
			final boolean honourFilter) {
		resetForRequest(true);
		requestImage(upload, false, null);
	}

	public void setFadeInDrawables(boolean fadeIn) {
		mFadeInDrawables = fadeIn;

		if (fadeIn && null == mFadeFromDrawable) {
			mFadeFromDrawable = new ColorDrawable(Color.TRANSPARENT);
			mFadeDuration = getResources().getInteger(
					android.R.integer.config_shortAnimTime);
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		if (mFadeInDrawables && null != drawable) {
			TransitionDrawable newDrawable = new TransitionDrawable(
					new Drawable[] { mFadeFromDrawable, drawable });
			super.setImageDrawable(newDrawable);
			newDrawable.startTransition(mFadeDuration);
		} else {
			super.setImageDrawable(drawable);
		}
	}

	private void requestImage(final GateReportImage upload,
			final boolean fullSize, final OnPhotoLoadListener listener) {
		// final String key = fullSize ? upload.getDisplayImageKey() : upload
		// .getThumbnailImageKey();
		// BitmapLruCache cache = CJayApplication.getApplication(getContext())
		// .getImageCache();
		// final CacheableBitmapWrapper cached = cache.get(key);
		//
		// if (null != cached && cached.hasValidBitmap()) {
		// setImageCachedBitmap(cached);
		// if (null != listener) {
		// listener.onPhotoLoadFinished(cached.getBitmap());
		// }
		// } else {
		// // Means we have an object with an invalid bitmap so remove it
		// if (null != cached) {
		// cache.remove(key);
		// }
		//
		// CJayApplication app = CJayApplication.getApplication(getContext());
		// mCurrentRunnable = app.getMultiThreadExecutorService().submit(
		// new PhotoLoadRunnable(this, upload, cache, fullSize,
		// listener));
		// }
	}

	private void resetForRequest(final boolean clearDrawable) {
		cancelRequest();

		// Clear currently display bitmap
		if (clearDrawable) {
			setImageDrawable(null);
		}
	}

}
