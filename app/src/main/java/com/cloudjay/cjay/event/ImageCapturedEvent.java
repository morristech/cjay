package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.util.enums.ImageType;

/**
 * User for update list of image capture and session in working fragment
 */
public class ImageCapturedEvent {
	private String containerId;
	private int imageType;
	private String auditItemUUID;
    private boolean isOpened;

	public ImageCapturedEvent(String containerId, int imageType, String auditItemUUID, boolean isOpened) {

		this.containerId = containerId;
		this.imageType = imageType;
		this.auditItemUUID = auditItemUUID;
        this.isOpened = isOpened;
	}

	public ImageCapturedEvent(String containerId, ImageType imageType,String auditItemUUID) {

		this.containerId = containerId;
		this.imageType = imageType.value;
		this.auditItemUUID = auditItemUUID;

	}

	public String getContainerId() {
		return containerId;
	}

	public int getImageType() {
		return this.imageType;
	}

	public String getAuditItemUUID() {
		return this.auditItemUUID;
	}

    public boolean isOpened() {
        return this.isOpened;
    }
}