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
package com.cloudjay.cjay.adapter;

import com.cloudjay.cjay.CJayApplication;
import com.cloudjay.cjay.PhotoUploadController;
import com.cloudjay.cjay.listener.OnSingleTapListener;
import com.cloudjay.cjay.model.PhotoUpload;
import com.cloudjay.cjay.util.CursorPagerAdapter;
import com.cloudjay.cjay.util.MediaStoreCursorHelper;
import com.cloudjay.cjay.view.MultiTouchImageView;
import com.cloudjay.cjay.view.PhotoTagItemLayout;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class UserPhotosViewPagerAdapter extends CursorPagerAdapter {

	private final PhotoUploadController mController;

	public UserPhotosViewPagerAdapter(Context context) {
		super(context, null, 0);

		CJayApplication app = CJayApplication.getApplication(context);
		mController = app.getPhotoUploadController();
	}

	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final PhotoUpload upload = MediaStoreCursorHelper
				.photosCursorToSelection(
						MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI, cursor);

		PhotoTagItemLayout view = new PhotoTagItemLayout(mContext, mController,
				upload);
		view.setPosition(cursor.getPosition());

		if (null != upload) {
			MultiTouchImageView imageView = view.getImageView();
			imageView.requestFullSize(upload, true, null);
		}

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// NO-OP
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		PhotoTagItemLayout view = (PhotoTagItemLayout) object;

		MultiTouchImageView imageView = view.getImageView();
		imageView.cancelRequest();

		((ViewPager) container).removeView(view);
	}

}
