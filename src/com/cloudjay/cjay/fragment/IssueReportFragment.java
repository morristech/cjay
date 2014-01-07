package com.cloudjay.cjay.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.cloudjay.cjay.model.Issue;

public abstract class IssueReportFragment extends SherlockFragment {
	public abstract void setIssue(Issue issue);
	public abstract void validateAndSaveData();
	public abstract void showKeyboard();
	public abstract void hideKeyboard();
}
