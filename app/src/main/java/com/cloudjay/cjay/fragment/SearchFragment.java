package com.cloudjay.cjay.fragment;



import android.os.Bundle;
<<<<<<< HEAD
import android.support.v4.app.Fragment;
=======
import android.support.v4.app.FragmentManager;
>>>>>>> 7a876b57b725e1a3c507603f935a13322ec460d9
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cloudjay.cjay.R;

import butterknife.InjectView;

import com.cloudjay.cjay.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SearchFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(int sectionNumber) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

<<<<<<< HEAD

=======
public class SearchFragment extends android.support.v4.app.Fragment {

	@InjectView(R.id.btn_search)
	Button btnSearch;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search, container, false);
		btnSearch = (Button) rootView.findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddContainerDialog();
			}
		});

		return rootView;
	}


	private void showAddContainerDialog() {
		FragmentManager fragmentManager = getChildFragmentManager();
		AddContainerDialog addContainerDialog = new AddContainerDialog(getActivity());
		addContainerDialog.show(fragmentManager, "fragment_addcontainer");
	}


>>>>>>> 7a876b57b725e1a3c507603f935a13322ec460d9
}
