package com.cloudjay.cjay.network;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.aerilys.helpers.android.NetworkHelper;
import com.aerilys.helpers.android.UIHelper;
import com.cloudjay.cjay.dao.ContainerSessionDaoImpl;
import com.cloudjay.cjay.dao.DamageCodeDaoImpl;
import com.cloudjay.cjay.dao.OperatorDaoImpl;
import com.cloudjay.cjay.dao.RepairCodeDaoImpl;
import com.cloudjay.cjay.model.CJayResourceStatus;
import com.cloudjay.cjay.model.ContainerSession;
import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.IDatabaseManager;
import com.cloudjay.cjay.model.Operator;
import com.cloudjay.cjay.model.RepairCode;
import com.cloudjay.cjay.model.TmpContainerSession;
import com.cloudjay.cjay.model.User;
import com.cloudjay.cjay.util.CJayConstant;
import com.cloudjay.cjay.util.Logger;
import com.cloudjay.cjay.util.Mapper;
import com.cloudjay.cjay.util.PreferencesUtil;
import com.cloudjay.cjay.util.Session;
import com.cloudjay.cjay.util.StringHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * CJay Network module
 * 
 * @author tieubao
 * 
 */
@SuppressLint("SimpleDateFormat")
public class CJayClient implements ICJayClient {

	private static final String LOG_TAG = "CJayClient";
	public static String BASE_URL = "https://cloudjay-web.appspot.com/api/jaypix/";

	private IHttpRequestWrapper requestWrapper;
	private IDatabaseManager databaseManager;
	private static CJayClient instance = null;

	public IHttpRequestWrapper getRequestWrapper() {
		return requestWrapper;
	}

	public void setRequestWrapper(IHttpRequestWrapper requestWrapper) {
		this.requestWrapper = requestWrapper;
	}

	public IDatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(IDatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	private CJayClient() {
	}

	private CJayClient(IHttpRequestWrapper requestWrapper,
			IDatabaseManager databaseManager) {
		this.requestWrapper = requestWrapper;
		this.databaseManager = databaseManager;
	}

	public static CJayClient getInstance() {
		if (instance == null) {
			instance = new CJayClient();
		}
		return instance;
	}

	public void init(IHttpRequestWrapper requestWrapper,
			IDatabaseManager databaseManager) {
		instance = new CJayClient(requestWrapper, databaseManager);
	}

	private HashMap<String, String> prepareHeadersWithToken(Context ctx) {

		User currentUser = null;
		currentUser = Session.restore(ctx).getCurrentUser();

		// User currentUser = ((CJayActivity) ctx).getCurrentUser();
		HashMap<String, String> headers = new HashMap<String, String>();
		String accessToken = currentUser.getAccessToken();
		headers.put("Authorization", "Token " + accessToken);
		return headers;
	}

	private HashMap<String, String> prepareHeadersWithToken(String token) {

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		return headers;
	}

	/**
	 * 
	 * fetch data based on current user role
	 * 
	 * - GATE: no need to GET ContainerSession from Server
	 * 
	 * - AUDITOR:
	 * 
	 * @param ctx
	 */
	public void fetchData(Context ctx) {
		Logger.Log(LOG_TAG, "fetching data ...");

		try {

			Date now = new Date();

			// 2013-11-10T21:05:24+08:00
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZZ");
			String nowString = dateFormat.format(now);

			// 1. chưa có data
			Logger.Log(LOG_TAG, "no iso code");
			OperatorDaoImpl operatorDaoImpl = databaseManager.getHelper(ctx)
					.getOperatorDaoImpl();
			DamageCodeDaoImpl damageCodeDaoImpl = databaseManager
					.getHelper(ctx).getDamageCodeDaoImpl();
			RepairCodeDaoImpl repairCodeDaoImpl = databaseManager
					.getHelper(ctx).getRepairCodeDaoImpl();

			if (operatorDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list operators");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_OPERATOR_LAST_UPDATE,
						nowString);

				List<Operator> operators = getOperators(ctx);
				if (null != operators)
					operatorDaoImpl.addListOperators(operators);
			}

			if (damageCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list damage codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_DAMAGE_LAST_UPDATE, nowString);
				List<DamageCode> damageCodes = getDamageCodes(ctx);
				if (null != damageCodes)
					damageCodeDaoImpl.addListDamageCodes(damageCodes);
			}

			if (repairCodeDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get list repair codes");
				PreferencesUtil.storePrefsValue(ctx,
						PreferencesUtil.RESOURCE_REPAIR_LAST_UPDATE, nowString);

				List<RepairCode> repairCodes = getRepairCodes(ctx);
				if (null != repairCodes)
					repairCodeDaoImpl.addListRepairCodes(repairCodes);
			}

			// 2. fetch ISO CODE
			if (hasNewMetadata(ctx)) {
				Logger.Log(LOG_TAG, "fetch iso code");
			}

			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			// 3. Update list ContainerSessions
			Logger.Log(LOG_TAG, "get list container sessions");
			List<ContainerSession> containerSessions = null;
			if (containerSessionDaoImpl.isEmpty()) {
				Logger.Log(LOG_TAG, "get new list container sessions");
				containerSessions = getContainerSessions(ctx);
			} else {
				Logger.Log(LOG_TAG, "get updated list container sessions");
				Date date = new Date();
				containerSessions = getContainerSessions(ctx, date);

				if (containerSessions == null) {
					Logger.Log(LOG_TAG, "No new container session");
				}
			}
			containerSessionDaoImpl.addListContainerSessions(containerSessions);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUserToken(String username, String password, Context ctx)
			throws JSONException {

		Logger.Log("getting User Token ... ");

		JSONObject requestPacket = new JSONObject();
		requestPacket.put("username", username);
		requestPacket.put("password", password);

		Logger.Log(CJayConstant.TOKEN);

		String tokenResponseString = requestWrapper.sendJSONPost(
				CJayConstant.TOKEN, requestPacket);

		JsonElement jelement = new JsonParser().parse(tokenResponseString);

		String token = null;
		try {
			token = jelement.getAsJsonObject().get("token").getAsString();
		} catch (Exception ex) {
			token = null;
		}
		return token;
	}

	@Override
	public String getGoogleCloudToken(String token) {
		HashMap<String, String> headers = prepareHeadersWithToken(token);
		String response = requestWrapper.sendGet(
				CJayConstant.API_GOOGLE_CLOUD_STORAGE_TOKEN, headers);
		return response;
	}

	@Override
	public void addGCMDevice(String regid, Context ctx) throws JSONException {
		JSONObject requestPacket = new JSONObject();
		requestPacket.put("registration_id", regid);
		String androidId = Secure.getString(ctx.getContentResolver(),
				Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
		String deviceId = deviceUuid.toString();
		requestPacket.put("device_id", deviceId);
		requestPacket.put("name", android.os.Build.MODEL);

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = requestWrapper.sendJSONPost(
				CJayConstant.API_ADD_GCM_DEVICE, requestPacket, headers);
		Log.i("GCM", response);
	}

	@Override
	public User getCurrentUser(String token, Context ctx) {
		Logger.Log("getting Current User ...");

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Token " + token);
		String response = requestWrapper.sendGet(CJayConstant.CURRENT_USER,
				headers);

		Logger.Log(response);
		Gson gson = new Gson();
		Type userType = new TypeToken<User>() {
		}.getType();

		User user = gson.fromJson(response, userType);
		return user;
	}

	@Override
	public List<Operator> getOperators(Context ctx) {

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS,
				headers);

		response = requestWrapper.sendGet(CJayConstant.LIST_OPERATORS, headers);
		Logger.Log(LOG_TAG, response);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Operator>>() {
		}.getType();

		List<Operator> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<DamageCode> getDamageCodes(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_DAMAGE_CODES, headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<DamageCode>>() {
		}.getType();

		List<DamageCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<RepairCode> getRepairCodes(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_REPAIR_CODES, headers);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<RepairCode>>() {
		}.getType();

		List<RepairCode> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.LIST_CONTAINER_SESSIONS, headers);

		Logger.Log(LOG_TAG, response);

		Gson gson = new GsonBuilder().setDateFormat(
				CJayConstant.CJAY_DATETIME_FORMAT).create();

		Type listType = new TypeToken<List<TmpContainerSession>>() {
		}.getType();

		List<TmpContainerSession> tmpContainerSessions = null;
		try {
			tmpContainerSessions = gson.fromJson(response, listType);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Parse to `ContainerSession`
		List<ContainerSession> items = new ArrayList<ContainerSession>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			if (null != tmpContainerSessions) {
				for (TmpContainerSession tmpSession : tmpContainerSessions) {
					ContainerSession containerSession = Mapper
							.toContainerSession(tmpSession, ctx);

					if (null != containerSession) {
						containerSessionDaoImpl
								.addContainerSessions(containerSession);
						items.add(containerSession);
					}

					Logger.Log(LOG_TAG, containerSession.toString());
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, Date date) {
		List<ContainerSession> items = new ArrayList<ContainerSession>();
		String formatedDate = StringHelper.getTimestamp(
				CJayConstant.CJAY_DATETIME_FORMAT, date);
		items = getContainerSessions(ctx, formatedDate);
		return items;
	}

	@Override
	public List<ContainerSession> getContainerSessions(Context ctx, String date) {

		HashMap<String, String> headers = prepareHeadersWithToken(ctx);

		String response = requestWrapper.sendGet(String.format(
				CJayConstant.LIST_CONTAINER_SESSIONS_WITH_DATETIME, date),
				headers);

		Gson gson = new GsonBuilder().setDateFormat(
				CJayConstant.CJAY_DATETIME_FORMAT).create();

		Type listType = new TypeToken<List<TmpContainerSession>>() {
		}.getType();

		List<TmpContainerSession> tmpContainerSessions = null;
		try {
			tmpContainerSessions = gson.fromJson(response, listType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Parse to `ContainerSession`
		List<ContainerSession> items = new ArrayList<ContainerSession>();
		try {
			ContainerSessionDaoImpl containerSessionDaoImpl = databaseManager
					.getHelper(ctx).getContainerSessionDaoImpl();

			if (tmpContainerSessions != null) {
				for (TmpContainerSession tmpSession : tmpContainerSessions) {
					ContainerSession containerSession = Mapper
							.toContainerSession(tmpSession, ctx);

					if (null != containerSession) {
						containerSessionDaoImpl
								.addContainerSessions(containerSession);
						items.add(containerSession);
					}
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return items;
	}

	@Override
	public List<Operator> getOperators(Context ctx, Date date) {
		return null;
	}

	@Override
	public List<Operator> getOperators(Context ctx, String date) {
		return null;
	}

	@Override
	public boolean hasNewMetadata(Context ctx) {
		return false;
	}

	@Override
	public List<CJayResourceStatus> getCJayResourceStatus(Context ctx) {
		HashMap<String, String> headers = prepareHeadersWithToken(ctx);
		String response = requestWrapper.sendGet(
				CJayConstant.CJAY_RESOURCE_STATUS, headers);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<CJayResourceStatus>>() {
		}.getType();

		List<CJayResourceStatus> items = gson.fromJson(response, listType);
		return items;
	}

	@Override
	public void postContainerSession(Context ctx, TmpContainerSession item) {

		try {
			if (NetworkHelper.isConnected(ctx)) {
				HashMap<String, String> headers = prepareHeadersWithToken(ctx);
				Gson gson = new Gson();
				String data = gson.toJson(item);
				String url = CJayConstant.CJAY_ITEMS;
				requestWrapper.sendPost(url, data, "application/json", headers);
			} else {
				Logger.Log("Network is not available");
				UIHelper.toast(ctx, "Network is not available");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
