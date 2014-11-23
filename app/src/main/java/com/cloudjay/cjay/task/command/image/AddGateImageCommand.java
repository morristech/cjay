package com.cloudjay.cjay.task.command.image;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.activity.CameraActivity;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.cloudjay.cjay.task.command.Command;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

public class AddGateImageCommand extends Command {

	Context context;
	GateImage image;
	String containerId;

	public AddGateImageCommand(Context context, GateImage gateImage, String containerId) {
		this.containerId = containerId;
		this.context = context;
		this.image = gateImage;
	}

	@Override
	protected void run() {
		DataCenter dataCenter = DataCenter_.getInstance_(context);
		dataCenter.addGateImage(context, image, containerId);
	}
}
