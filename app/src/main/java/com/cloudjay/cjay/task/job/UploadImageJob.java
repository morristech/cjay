package com.cloudjay.cjay.task.job;

import android.content.Context;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.DataCenter;
import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.event.upload.UploadStoppedEvent;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.image.ChangeImageUploadStatusCommand;
import com.cloudjay.cjay.task.command.log.AddLogCommand;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Priority;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.UploadStatus;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UploadImageJob extends Job {
	String containerId;
	String uri;
	String imageName;
	ImageType imageType;
	UploadObject object;

	@Override
	public int getRetryLimit() {
		return CJayConstant.RETRY_THRESHOLD;
	}

	public UploadImageJob(String uri, String imageName, String containerId, ImageType imageType, UploadObject object) {
		super(new Params(Priority.MID).requireNetwork().persist().groupBy(containerId).setPersistent(true));

		this.containerId = containerId;
		this.uri = uri;
		this.imageName = imageName;
		this.imageType = imageType;
		this.object = object;
	}

	@Override
	public void onAdded() {
	}

	@Override
	public void onRun() throws Throwable {

		Logger.Log("Upload img: " + imageName);
		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		// Call data center to upload image
		Response response = dataCenter.uploadImage(uri, imageName);

		// Change image status to COMPLETE and also notify success to Upload Fragment by posting UploadSucceededEvent in ChangeImageUploadStatusCommand
		dataCenter.add(new ChangeImageUploadStatusCommand(context, containerId, imageName, imageType, UploadStatus.COMPLETE, object));
	}

	@Override
	protected void onCancel() {

		// Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
		// Set image upload Status is ERROR and notify to Upload Fragment
		Context context = App.getInstance().getApplicationContext();
		DataCenter dataCenter = DataCenter_.getInstance_(context);

		dataCenter.add(new AddLogCommand(context, containerId, "Không thể tải lên hình: " + imageName, CJayConstant.PREFIX_LOG));
		dataCenter.add(new ChangeImageUploadStatusCommand(context, containerId, imageName, imageType, UploadStatus.ERROR, object));
		EventBus.getDefault().post(new UploadStoppedEvent(containerId));
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		//if it is a 4xx error, stop
		if (throwable instanceof RetrofitError) {

			RetrofitError retrofitError = (RetrofitError) throwable;
			Logger.Log("Retrofit response: " + retrofitError.getBody().toString());
			return retrofitError.getResponse().getStatus() < 400 || retrofitError.getResponse().getStatus() > 499;

		}

		return true;
	}
}
