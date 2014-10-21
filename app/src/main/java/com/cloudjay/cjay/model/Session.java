package com.cloudjay.cjay.model;


import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.enums.ImageType;
import com.cloudjay.cjay.util.enums.Status;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.annotation.Generated;


@Generated("org.jsonschema2pojo")
public class Session {

    public static final String FIELD_CONTAINER_ID = "container_id";

    @Expose
    private long id;

    @Expose
    private long step;

    @SerializedName("pre_status")
    @Expose
    private long preStatus;

    @Expose
    private long status;

    @SerializedName(FIELD_CONTAINER_ID)
    @Expose
    private String containerId;

    @SerializedName("operator_code")
    @Expose
    private String operatorCode;

    @SerializedName("operator_id")
    @Expose
    private long operatorId;

    @SerializedName("depot_code")
    @Expose
    private String depotCode;

    @SerializedName("depot_id")
    @Expose
    private long depotId;

    @SerializedName("check_in_time")
    @Expose
    private String checkInTime;

    public static String getFieldContainerId() {
        return FIELD_CONTAINER_ID;
    }

    @SerializedName("check_out_time")
    @Expose
    private String checkOutTime;

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean isUploaded) {
        this.uploaded = isUploaded;
    }

    public Session withUploaded(boolean isUploaded) {
        this.uploaded = isUploaded;
        return this;
    }

    private boolean uploaded;

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean isProcessing) {
        this.processing = isProcessing;
    }

    public Session withProcessing(boolean isProcessing) {
        this.processing = isProcessing;
        return this;
    }

    private boolean processing;

    @SerializedName("gate_images")
    @Expose
    private List<GateImage> gateImages;

    @SerializedName("audit_items")
    @Expose
    private List<AuditItem> auditItems;

    @SerializedName("audit_images")
    @Expose
    private List<AuditImage> auditImages;

    public Session() {
    }

    /**
     * Get session in Json type to upload
     *
     * @return
     */
    public JsonObject getJsonSession() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pre_status", this.preStatus);
        jsonObject.addProperty("container_id", containerId);
        jsonObject.addProperty("operator_id", operatorId);
        jsonObject.add("gate_images", this.getGateInImageToUpLoad());
        return jsonObject;
    }

    /**
     * Return JSONArray of gate-in image list look like [{name: '....'}, {name: '....'}, ...] for upload
     *
     * @return
     * @throws JSONException
     */
    public JsonArray getGateInImageToUpLoad() {
        JsonArray gate_image = new JsonArray();
        for (GateImage gateImage : this.gateImages) {
            if (gateImage.getType() == ImageType.IMPORT.getValue()) {
                String gateImageName = gateImage.getName();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", gateImageName);
                gate_image.add(jsonObject);
            }
        }
        return gate_image;
    }

    /**
     * Return JSONArray of gate-out image list look like [{name: '....'}, {name: '....'}, ...] for upload
     *
     * @return
     * @throws JSONException
     */
    public JsonArray getGateOutImageToUpLoad() {
        JsonArray gate_image = new JsonArray();
        for (GateImage gateImage : this.gateImages) {
            if (gateImage.getType() == ImageType.EXPORT.getValue()) {
                String gateImageName = gateImage.getName();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", gateImageName);
                gate_image.add(jsonObject);
            }
        }
        return gate_image;
    }


    /**
     * Return JSONArray of audit items list look like [{id: xx, audit_images: [{ name : '...', ...}]}, ...] for upload
     *
     * @return
     * @throws JSONException
     */
    public JsonArray getRepairedAuditItemToUpLoad() {
        JsonArray auditItems = new JsonArray();
        for (AuditItem auditItem : this.auditItems) {
            JsonObject jsonObject = new JsonObject();
            long auditId = auditItem.getId();
            jsonObject.addProperty("name", auditId);
            auditItems.add(jsonObject);
            JsonArray repairedImageName = auditItem.getRepairedImageToUpLoad();
            jsonObject.add("audit_images", repairedImageName);
        }
        return auditItems;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Session withId(long id) {
        this.id = id;
        return this;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public Session withStep(long step) {
        this.step = step;
        return this;
    }

    public long getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(long preStatus) {
        this.preStatus = preStatus;
    }

    public Session withPreStatus(long preStatus) {
        this.preStatus = preStatus;
        return this;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public Session withStatus(long status) {
        this.status = status;
        return this;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Session withContainerId(String containerId) {
        this.containerId = containerId;
        return this;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public Session withOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
        return this;
    }

    public long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(long operatorId) {
        this.operatorId = operatorId;
    }

    public Session withOperatorId(long operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    public String getDepotCode() {
        return depotCode;
    }

    public void setDepotCode(String depotCode) {
        this.depotCode = depotCode;
    }

    public Session withDepotCode(String depotCode) {
        this.depotCode = depotCode;
        return this;
    }

    public long getDepotId() {
        return depotId;
    }

    public void setDepotId(long depotId) {
        this.depotId = depotId;
    }

    public Session withDepotId(long depotId) {
        this.depotId = depotId;
        return this;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Session withCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
        return this;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Session withCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
        return this;
    }

    public List<GateImage> getGateImages() {
        return gateImages;
    }

    public void setGateImages(List<GateImage> gateImages) {
        this.gateImages = gateImages;
    }

    public Session withGateImages(List<GateImage> gateImages) {
        this.gateImages = gateImages;
        return this;
    }

    public List<AuditItem> getAuditItems() {
        return auditItems;
    }

    public void setAuditItems(List<AuditItem> auditItems) {
        this.auditItems = auditItems;
    }

    public Session withAuditItems(List<AuditItem> auditItems) {
        this.auditItems = auditItems;
        return this;
    }

    public List<AuditImage> getAuditImages() {
        return auditImages;
    }

    public void setAuditImages(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
    }

    public Session withAuditImages(List<AuditImage> auditImages) {
        this.auditImages = auditImages;
        return this;
    }

}