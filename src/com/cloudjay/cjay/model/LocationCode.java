package com.cloudjay.cjay.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Danh sách vị trí.
 * 
 * @author tieubao
 * 
 */
@DatabaseTable(tableName = "location_code")
public class LocationCode {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String CODE = "code";

	@DatabaseField(id = true, columnName = ID)
	private String id;

	@DatabaseField(columnName = NAME)
	private String name;

	@DatabaseField(columnName = CODE)
	private String code;
	
	// @ForeignCollectionField(eager = true)
	// private ForeignCollection<Issue> issues;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String operatorId) {
		this.id = operatorId;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
