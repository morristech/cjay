package com.cloudjay.cjay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.Session;

import java.util.List;

/**
 * Created by thai on 09/10/2014.
 */
public class UploadSessionAdapter extends ArrayAdapter<Session> {

    private LayoutInflater mInflater;
    private int layoutResId;

    public UploadSessionAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResId = resource;
    }

    private static class ViewHolder {
        TextView tvContainerId;
        TextView tvOperator;
        TextView tvDateIn;
        TextView tvDateOut;
        TextView tvDateEstimate;
        TextView tvDateRepair;
        TextView tvPreStatus;
        TextView tvCurrentStatus;
        TextView tvStep;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Session session = getItem(position);
        // Apply ViewHolder pattern for better performance
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.tvContainerId = (TextView) convertView.findViewById(R.id.tv_container_id);
            viewHolder.tvOperator = (TextView) convertView.findViewById(R.id.tv_operator);
            viewHolder.tvDateIn = (TextView) convertView.findViewById(R.id.tv_date_in);
            viewHolder.tvDateOut = (TextView) convertView.findViewById(R.id.tv_date_out);
            viewHolder.tvDateEstimate = (TextView) convertView.findViewById(R.id.tv_date_estimate);
            viewHolder.tvDateRepair = (TextView) convertView.findViewById(R.id.tv_date_repair);
            viewHolder.tvPreStatus = (TextView) convertView.findViewById(R.id.tv_pre_status);
            viewHolder.tvCurrentStatus = (TextView) convertView.findViewById(R.id.tv_current_status);
            viewHolder.tvStep = (TextView) convertView.findViewById(R.id.tv_step);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //Set data to view
        viewHolder.tvContainerId.setText(session.getContainerId());
        viewHolder.tvOperator.setText(session.getOperatorCode());
        viewHolder.tvDateIn.setText(String.valueOf(session.getCheckInTime()));
        viewHolder.tvDateOut.setText(String.valueOf(session.getCheckOutTime()));
        viewHolder.tvStep.setText(String.valueOf(session.getStep()));
        viewHolder.tvPreStatus.setText(String.valueOf(session.getPreStatus()));
        viewHolder.tvCurrentStatus.setText(String.valueOf(session.getStatus()));

        return convertView;
    }

    public void setData(List<Session> data) {
        clear();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                add(data.get(i));
            }
        }
    }
}
