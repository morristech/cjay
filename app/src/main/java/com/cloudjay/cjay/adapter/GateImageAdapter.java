package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.view.CheckablePhotoGridItemLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

public class GateImageAdapter extends ArrayAdapter<GateImage> {

	private LayoutInflater inflater;
	private int resource;
	private boolean mCheckable = false;
	private ArrayList<GateImage> mArrayCheckedImages;

	public GateImageAdapter(Context context, int resource, boolean isCheckable) {
		super(context, resource);

		this.inflater = LayoutInflater.from(context);
		this.mCheckable = isCheckable;
		this.resource = resource;

		mArrayCheckedImages = new ArrayList<>();
	}

	private class ViewHolder {
		public ImageView ivGateImage;
		public CheckablePhotoGridItemLayout photoLayout;
	}

	@Override
	public View getView(final int i, View convertView, ViewGroup viewGroup) {

		final GateImage gateImage = getItem(i);

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(resource, null);
			holder.ivGateImage = (ImageView) convertView.findViewById(R.id.iv_image);
			holder.photoLayout = (CheckablePhotoGridItemLayout) convertView.findViewById(R.id.photo_layout);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CheckablePhotoGridItemLayout layout = holder.photoLayout;
		layout.setShowCheckbox(mCheckable);
		if (mCheckable) {
			layout.setParentAdapter(this);
			layout.setCJayImage(gateImage);
			layout.setChecked(mArrayCheckedImages.contains(gateImage));
		}

		ImageAware imageAware = new ImageViewAware(holder.ivGateImage, false);
		ImageLoader.getInstance().displayImage(gateImage.getUrl(), imageAware);
		return convertView;
	}

	public void addCheckedCJayImageUrl(GateImage gateImage) {
		mArrayCheckedImages.add(gateImage);
	}

	public void removeCheckedCJayImageUrl(GateImage gateImage) {
		mArrayCheckedImages.remove(gateImage);
	}

	public void setCheckedCJayImageUrls(ArrayList<GateImage> checkedCJayImageUrls) {
		mArrayCheckedImages = checkedCJayImageUrls;
	}

	public List<GateImage> getCheckedCJayImageUrls() {
		return mArrayCheckedImages;
	}

	public int getCheckedCJayImageUrlsCount() {
		return mArrayCheckedImages.size();
	}

	public void removeAllCheckedCJayImageUrl() {
		mArrayCheckedImages.clear();
	}

	public void setData(List<GateImage> data) {
		this.clear();
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				this.insert(data.get(i), this.getCount());
			}
		}
		this.notifyDataSetChanged();
	}
}
