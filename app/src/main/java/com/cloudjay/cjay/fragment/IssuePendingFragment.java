package com.cloudjay.cjay.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.activity.CameraActivity_;
import com.cloudjay.cjay.adapter.AuditItemAdapter;
import com.cloudjay.cjay.event.ImageCapturedEvent;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.Status;
import com.cloudjay.cjay.util.enums.Step;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_issue_pending)
public class IssuePendingFragment extends Fragment {

	public final static String CONTAINER_ID_EXTRA = "com.cloudjay.wizard.containerID";

	@FragmentArg(CONTAINER_ID_EXTRA)
	public String containerID;

	@ViewById(R.id.tv_container_code)
	TextView tvContainerId;

	@ViewById(R.id.tv_current_status)
	TextView tvCurrentStatus;

	@ViewById(R.id.btn_camera)
	LinearLayout btnCamera;

    @ViewById(R.id.btn_clean)
    Button btnClean;

	@ViewById(R.id.lv_audit_items)
	ListView lvAuditItems;

	@Bean
	DataCenter dataCenter;

	String operatorCode;
    long currentStatus;
	AuditItemAdapter auditItemAdapter;

    Session mSession;

	public IssuePendingFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@AfterViews
	void setUp() {

        // Get session by containerId
        mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerID);

        if (mSession != null) {
            // Get operator code
            containerID = mSession.getContainerId();
            operatorCode = mSession.getOperatorCode();

            // Set currentStatus to TextView
            currentStatus = mSession.getStatus();
            tvCurrentStatus.setText((Status.values()[(int) currentStatus]).toString());

            // Set ContainerId to TextView
            tvContainerId.setText(containerID);

            auditItemAdapter = new AuditItemAdapter(getActivity(),
                    R.layout.item_issue_pending, containerID);
            lvAuditItems.setAdapter(auditItemAdapter);

            refresh();
        } else {
            // Set ContainerId to TextView
            tvContainerId.setText(containerID);
        }
	}

    @UiThread
    void onEvent(ImageCapturedEvent event) {

        Logger.Log("on ImageCapturedEvent");

        // Re-query container session with given containerId
        String containerId = event.getContainerId();
        mSession = dataCenter.getSession(getActivity().getApplicationContext(), containerId);
        refresh();
    }

	@Click(R.id.btn_camera)
	void buttonCameraClicked() {
		// Open camera activity
		Intent cameraActivityIntent = new Intent(getActivity(), CameraActivity_.class);
		cameraActivityIntent.putExtra(CameraFragment.CONTAINER_ID_EXTRA, containerID);
		cameraActivityIntent.putExtra(CameraFragment.IMAGE_TYPE_EXTRA, CJayConstant.TYPE_AUDIT);
		cameraActivityIntent.putExtra(CameraFragment.OPERATOR_CODE_EXTRA, operatorCode);
		cameraActivityIntent.putExtra(CameraFragment.CURRENT_STEP_EXTRA, Step.AUDIT.value);
		startActivity(cameraActivityIntent);
	}

	@ItemClick(R.id.lv_audit_items)
	void showApproveDiaglog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_search_container_title);
		builder.setMessage("Lỗi này đã chưa được. Sửa luôn?");

		builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				//TODO show chon loi da giam dinh @Nam
				dialogInterface.dismiss();
			}
		});

		builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				//TODO add to database
				dialogInterface.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {

				// Set background and text color for confirm button
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE)
						.setTextColor(getActivity().getResources().getColor(android.R.color.holo_green_dark));
				((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE)
						.setBackgroundResource(getActivity().getResources().getColor(android.R.color.darker_gray));
			}
		});
		dialog.show();
	}

    @Background
    void refresh() {
        if (mSession != null) {
            List<AuditItem> list = mSession.getAuditItems();
            Logger.Log("Size: " + list.size());
            updatedData(list);
        }
    }

    void updatedData(List<AuditItem> auditItems) {
        auditItemAdapter.clear();

        if (auditItems != null) {
            for (AuditItem auditItem : auditItems) {
                auditItemAdapter.add(auditItem);
            }
        }

        auditItemAdapter.notifyDataSetChanged();

        // If container has audit image(s), hide button Container Ve sinh - quet
        if (auditItemAdapter.getCount() > 0) {
            btnClean.setVisibility(View.GONE);
        }
    }

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
}
