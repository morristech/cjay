package com.cloudjay.cjay.model;

public class GateReportImage {

	private int id;
	private int type;
	private String created_at;
	private String image_name;
	private String image_url;

	public GateReportImage() {

	}

	public GateReportImage(int id, int type, String time_posted, String image_name, String image_url) {

		this(type, time_posted, image_name, image_url);
		this.id = id;

		// this.type = type;
		// created_at = time_posted;
		// this.image_name = image_name;
		// this.image_url = image_url;

	}

	public GateReportImage(int type, String time_posted, String image_name, String image_url) {

		this.type = type;
		created_at = time_posted;
		this.image_name = image_name;
		this.image_url = image_url;

	}

	public String getCreatedAt() {
		return created_at;
	}

	public int getId() {
		return id;
	}

	public String getImageName() {
		return image_name;
	}

	public int getType() {
		return type;
	}

	public void setCreatedAt(String time_posted) {
		created_at = time_posted;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImageUrl() {
		return image_url;
	}

	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}
}
