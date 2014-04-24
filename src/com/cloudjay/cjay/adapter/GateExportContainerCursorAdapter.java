package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Container;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.ContainerState;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.StringHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GateExportContainerCursorAdapter extends CursorAdapter implements Filterable {

	private static class ViewHolder {

		public TextView containerIdView;
		public TextView containerOwnerView;
		public TextView importDateView;
		public TextView exportDateView;
		public ImageView itemPictureView;
		public ImageView validationImageView;
		public ImageView warningImageView;

	}

	private int layout;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	public boolean isScrolling;

	@SuppressWarnings("deprecation")
	public GateExportContainerCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public GateExportContainerCursorAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, c, flags);
		this.layout = layout;
		inflater = LayoutInflater.from(context);
		mCursor = c;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (cursor == null) {
			Logger.Log("-----> BUG");
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			Logger.Log("Holder inside bindView is NULL");

			holder = new ViewHolder();
			holder.containerIdView = (TextView) view.findViewById(R.id.feed_item_container_id);
			holder.containerOwnerView = (TextView) view.findViewById(R.id.feed_item_container_owner);
			holder.importDateView = (TextView) view.findViewById(R.id.feed_item_container_import_date);
			holder.exportDateView = (TextView) view.findViewById(R.id.feed_item_container_export_date);
			holder.itemPictureView = (ImageView) view.findViewById(R.id.feed_item_picture);
			holder.validationImageView = (ImageView) view.findViewById(R.id.feed_item_validator);
			holder.warningImageView = (ImageView) view.findViewById(R.id.feed_item_warning);
			view.setTag(holder);
		}

		ContainerState state = ContainerState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_SERVER_STATE))];
		if (state == ContainerState.IMPORTED || state == ContainerState.REPAIRED) {
			holder.warningImageView.setVisibility(View.GONE);
			view.setEnabled(false);

		} else {
			holder.warningImageView.setVisibility(View.VISIBLE);
			view.setEnabled(true);

		}

		String containerId = cursor.getString(cursor.getColumnIndexOrThrow(Container.CONTAINER_ID));
		holder.containerIdView.setText(containerId);

		// get data from cursor and bind to holder
		String importDate = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_CHECK_IN_TIME));
		importDate = StringHelper.getRelativeDate(CJayConstant.CJAY_DATETIME_FORMAT_NO_TIMEZONE, importDate);
		holder.importDateView.setText(importDate);

		// Logger.Log(containerId + " state: " + state.name());

		String operator = cursor.getString(cursor.getColumnIndexOrThrow(Operator.FIELD_NAME));
		holder.containerOwnerView.setText(operator);

		String url = cursor.getString(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_IMAGE_ID_PATH));
		// if (!TextUtils.isEmpty(url) && !url.equals("https://storage.googleapis.com/storage-cjay.cloudjay.com/")) {

		if (!TextUtils.isEmpty(url)
				&& !url.matches("^https://storage\\.googleapis\\.com/storage-cjay\\.cloudjay\\.com/\\s+$")) {
			imageLoader.displayImage(url, holder.itemPictureView);
		} else {
			holder.itemPictureView.setImageResource(R.drawable.ic_app);
		}

		boolean isValidForUpload = false;
		if (cursor.getColumnIndex("export_image_count") >= 0) {
			isValidForUpload = cursor.getInt(cursor.getColumnIndexOrThrow("export_image_count")) > 0;
		} else if (cursor.getColumnIndex("import_image_count") >= 0) {
			isValidForUpload = cursor.getInt(cursor.getColumnIndexOrThrow("import_image_count")) > 0;
		}
		if (isValidForUpload) {
			holder.validationImageView.setVisibility(View.VISIBLE);
		} else {
			holder.validationImageView.setVisibility(View.INVISIBLE);
		}

	}

	@Override
	public boolean isEnabled(int position) {

		// TODO: enable this block to disable wrong state container session
		Cursor cursor = (Cursor) getItem(position);
		ContainerState state = ContainerState.values()[cursor.getInt(cursor.getColumnIndexOrThrow(ContainerSession.FIELD_SERVER_STATE))];
		if (state == ContainerState.IMPORTED || state == ContainerState.REPAIRED) {

		} else {
			return false;
		}

		return super.isEnabled(position);
	}

	// get --> new --> bind
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View v = inflater.inflate(layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.containerIdView = (TextView) v.findViewById(R.id.feed_item_container_id);
		holder.containerOwnerView = (TextView) v.findViewById(R.id.feed_item_container_owner);
		holder.importDateView = (TextView) v.findViewById(R.id.feed_item_container_import_date);
		holder.exportDateView = (TextView) v.findViewById(R.id.feed_item_container_export_date);
		holder.itemPictureView = (ImageView) v.findViewById(R.id.feed_item_picture);
		holder.validationImageView = (ImageView) v.findViewById(R.id.feed_item_validator);
		holder.warningImageView = (ImageView) v.findViewById(R.id.feed_item_warning);

		v.setTag(holder);

		return v;
	}

}
