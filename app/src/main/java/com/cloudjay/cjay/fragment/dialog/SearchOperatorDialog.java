package com.cloudjay.cjay.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.widget.EditText;
import android.widget.ListView;

import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.adapter.OperatorAdapter;
import com.cloudjay.cjay.event.OperatorCallbackEvent;
import com.cloudjay.cjay.event.OperatorsGotEvent;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.util.Logger;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.realm.RealmResults;

@EFragment(R.layout.dialog_select_operator)
public class SearchOperatorDialog extends DialogFragment {

	private String mOperatorName;
	private Fragment mParent;

	RealmResults<Operator> operators;
	OperatorAdapter operatorAdapter;

	@ViewById(R.id.et_operator_name)
	EditText etOperatorName;

	@ViewById(R.id.lv_operators_list)
	ListView lvOperators;

	@Bean
	DataCenter dataCenter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@UiThread
	public void onEvent(OperatorsGotEvent event) {
		// retrieve list operators
		operators = event.getOperators();
        Logger.Log("operators count in event: " + operators.size());

		// Init and set adapter
        if (null == operatorAdapter) {
            Logger.Log("operatorAdapter is null");
            operatorAdapter = new OperatorAdapter(getActivity(), operators);
            lvOperators.setAdapter(operatorAdapter);
        }

        // Notify change
        operatorAdapter.swapOperators(operators);
        // operatorAdapter.notifyDataSetChanged();
	}

	@AfterViews
	void doAfterViews() {
		// Set title for search operator dialog
		getDialog().setTitle(getResources().getString(R.string.dialog_operator_title));
		// Begin to get operators from cache
		dataCenter.getOperators();
	}

	@ItemClick(R.id.lv_operators_list)
	void listViewOperatorsItemClicked(Operator selectedOperator) {
		EventBus.getDefault().post(new OperatorCallbackEvent(selectedOperator));
		this.dismiss();
	}

    @AfterTextChange(R.id.et_operator_name)
	void search(Editable text) {
        String keyword = text.toString();
		if (keyword.equals("")) {
			// Get all operators
            dataCenter.getOperators();
		} else {
			// Get operator(s) by keyword
            dataCenter.searchOperator(keyword.toUpperCase());
		}
	}

	public void setParent(Fragment parent) {
		mParent = parent;
	}
}
