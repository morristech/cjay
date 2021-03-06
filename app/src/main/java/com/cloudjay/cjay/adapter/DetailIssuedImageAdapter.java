package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.view.CheckableImageView;
import com.cloudjay.cjay.view.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by thai on 21/10/2014.
 */
public class DetailIssuedImageAdapter extends ArrayAdapter<AuditImage> {
    private LayoutInflater mInflater;
    private int layoutResId;
    Context context;
    ImageType type;

    /**
     * Create Adapter for list image view
     *
     * @param context
     * @param resource
     * @param type
     */
    public DetailIssuedImageAdapter(Context context, int resource, ImageType type) {
        super(context, resource);
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
        this.type = type;
    }

    private static class ViewHolder {
		SquareImageView imageView;
		CheckableImageView checkView;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AuditImage auditImage = getItem(position);

        // Apply ViewHolder pattern for better performance
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.imageView = (SquareImageView) convertView.findViewById(R.id.iv_image);
			viewHolder.checkView = (CheckableImageView) convertView.findViewById(R.id.cb_select);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Set data to view
		viewHolder.checkView.setVisibility(View.GONE);
        if (type == ImageType.values()[((int) auditImage.getType())]) {
            ImageLoader.getInstance().displayImage(auditImage.getUrl(), viewHolder.imageView);
        }

        return convertView;
    }

    public void setData(List<AuditImage> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
