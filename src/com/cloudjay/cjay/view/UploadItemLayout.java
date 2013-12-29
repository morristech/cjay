package com.cloudjay.cjay.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.events.UploadStateChangedEvent;
import com.cloudjay.cjay.model.ContainerSession;
import de.greenrobot.event.EventBus;

public class UploadItemLayout extends LinearLayout {

	private ContainerSession mSelection;

	public UploadItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		EventBus.getDefault().register(this);
	}

	public TextView getCaptionTextView() {
		return (TextView) findViewById(R.id.tv_photo_caption);
	}

	public PhotupImageView getImageView() {
		return (PhotupImageView) findViewById(R.id.iv_photo);
	}

	public ProgressBar getProgressBar() {
		return (ProgressBar) findViewById(R.id.pb_upload_progress);
	}

	public ImageView getResultImageView() {
		return (ImageView) findViewById(R.id.iv_upload_result);
	}

	public TextView getTagTextView() {
		return (TextView) findViewById(R.id.tv_photo_tags);
	}

	public void onEventMainThread(UploadStateChangedEvent event) {
		if (mSelection == event.getContainerSession()) {
			refreshUploadUi();
		}
	}

	public void refreshUploadUi() {
		if (null == mSelection) {
			return;
		}

		ProgressBar pb = getProgressBar();
		ImageView resultIv = getResultImageView();

		switch (mSelection.getUploadState()) {
		case ContainerSession.STATE_UPLOAD_COMPLETED:
			pb.setVisibility(View.GONE);
			resultIv.setImageResource(R.drawable.ic_success);
			resultIv.setVisibility(View.VISIBLE);
			break;

		case ContainerSession.STATE_UPLOAD_ERROR:
			pb.setVisibility(View.GONE);
			resultIv.setImageResource(R.drawable.ic_error);
			resultIv.setVisibility(View.VISIBLE);
			break;

		case ContainerSession.STATE_UPLOAD_IN_PROGRESS:
			pb.setVisibility(View.VISIBLE);
			resultIv.setVisibility(View.GONE);

			final int progress = mSelection.getUploadProgress();
			if (progress <= 0) {
				pb.setIndeterminate(true);
			} else {
				pb.setIndeterminate(false);
				pb.setProgress(progress);
			}
			break;

		case ContainerSession.STATE_UPLOAD_WAITING:
			pb.setVisibility(View.VISIBLE);
			resultIv.setVisibility(View.GONE);
			pb.setIndeterminate(true);
			break;
		}

		requestLayout();
	}

	public void setPhotoSelection(ContainerSession selection) {
		mSelection = selection;

		/**
		 * Initial UI Update
		 */
		PhotupImageView iv = getImageView();
		if (null != iv) {
			iv.requestThumbnail(mSelection, false);
		}

		/**
		 * Refresh Progress Bar
		 */
		refreshUploadUi();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		EventBus.getDefault().unregister(this);
	}

}
