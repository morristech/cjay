package com.cloudjay.cjay.task.command;

import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.task.service.QueryService_;
import com.google.gson.Gson;
import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.TaskQueue;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class CommandQueue extends TaskQueue<Command> {
	Context context;

	public CommandQueue(Context context) {
		super(new InMemoryObjectQueue<Command>());
		this.context = context;
	}

	private void startService() {
		context.startService(new Intent(context, QueryService_.class));
	}

	@Override
	public void add(Command entry) {
		super.add(entry);
		startService();
	}

	@Override
	public void remove() {
		super.remove();
	}
}