package com.cloudjay.cjay.fragment;



import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cloudjay.cjay.R;

/**
 * Màn hình nhập
 */
public class ImportFragment extends Fragment {

    Button btnContinue;

    public ImportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_import, container, false);
        btnContinue = (Button) v.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Go to next fragment */
                AuditFragment fragment = new AuditFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.ll_main_process, fragment);
                transaction.commit();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
