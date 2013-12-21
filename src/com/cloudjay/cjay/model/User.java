package com.cloudjay.cjay.model;

import com.cloudjay.cjay.dao.UserDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user", daoClass = UserDaoImpl.class)
public class User {

	public static final String ID = "id";
	public static final String USERNAME = "username";
	public static final String EMAIL = "email";
	public static final String IS_MAIN_ACCOUNT = "is_main_account";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String FULL_NAME = "full_name";
	public static final String ROLE_NAME = "role_name";
	public static final String ROLE = "role";
	public static final String AVATAR_URL = "avatar_url";
	public static final String DIALING_CODE = "dialing_code";
	public static final String PHONE = "phone";
	public static final String EXPIRE = "expire_in";

	@DatabaseField(id = true, columnName = ID)
	private int id;

	@DatabaseField(columnName = USERNAME)
	private String username;

	@DatabaseField(columnName = EMAIL)
	private String email;

	@DatabaseField(columnName = IS_MAIN_ACCOUNT, defaultValue = "0")
	private boolean is_main_account;

	@DatabaseField(columnName = ACCESS_TOKEN)
	private String access_token;

	@DatabaseField(columnName = FIRST_NAME)
	private String first_name;

	@DatabaseField(columnName = LAST_NAME)
	private String last_name;

	@DatabaseField(columnName = FULL_NAME)
	private String full_name;

	@DatabaseField(columnName = ROLE_NAME)
	private String role_name;

	@DatabaseField(columnName = ROLE, canBeNull = false)
	private int role;

	@DatabaseField(columnName = AVATAR_URL)
	private String avatar_url;

	@DatabaseField(columnName = DIALING_CODE)
	private int dialing_code;

	@DatabaseField(columnName = PHONE)
	private int phone;

	@DatabaseField(columnName = EXPIRE)
	private int expire_in;

	public int getExpire() {
		return expire_in;
	}

	public void setExpire(int day) {
		this.expire_in = day;
	}

	public String getAccessToken() {
		return access_token;
	}

	public void setAccessToken(String accessToken) {
		this.access_token = accessToken;
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String userName) {
		this.username = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return first_name;
	}

	public void setFirstName(String firstName) {
		this.first_name = firstName;
	}

	public String getLastName() {
		return last_name;
	}

	public void setLastName(String lastName) {
		this.last_name = lastName;
	}

	public boolean isMainAccount() {
		return is_main_account;
	}

	public void setMainAccount(boolean isMainAccount) {
		this.is_main_account = isMainAccount;
	}

	public String getFullName() {
		return full_name;
	}

	public void setFullName(String fullName) {
		this.full_name = fullName;
	}

	public String getAvatar() {
		return this.avatar_url;
	}

	public void setAvatar(String avatarUrl) {
		this.avatar_url = avatarUrl;
	}

	public String getRoleName() {
		return role_name;
	}

	public void setRoleName(String roleName) {
		this.role_name = roleName;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public int getDialingCode() {
		return dialing_code;
	}

	public void setDialingCode(int dialingCode) {
		this.dialing_code = dialingCode;
	}

	public int getPhone() {
		return phone;
	}

	public void setPhone(int phone) {
		this.phone = phone;
	}
}