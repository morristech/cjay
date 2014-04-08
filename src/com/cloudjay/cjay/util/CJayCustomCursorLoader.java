package com.cloudjay.cjay.util;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public abstract class CJayCustomCursorLoader extends CursorLoader {

	private Cursor mCursor;
	protected final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

	public CJayCustomCursorLoader(Context context) {
		super(context);
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor) {

		if (isReset()) {

			// An async query came in while the loader is stopped
			if (cursor != null) {
				cursor.close();
			}
			return;
		}

		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	/* Runs on a worker thread */
	@Override
	public abstract Cursor loadInBackground();

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
	}

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is
	 * ready the callbacks will be called on the UI thread. If a previous load
	 * has been completed and is still valid the result may be passed to the
	 * callbacks immediately.
	 * <p/>
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

}
