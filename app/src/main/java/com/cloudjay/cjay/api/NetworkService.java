package com.cloudjay.cjay.api;

import com.cloudjay.cjay.model.Session;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface NetworkService {

	@FormUrlEncoded
	@POST(ApiEndpoint.TOKEN_API)
	public JsonObject getToken(@Field("username") String username, @Field("password") String password);

	@GET(ApiEndpoint.CURRENT_USER_API)
	public JsonObject getCurrentUser();

	@GET(ApiEndpoint.LIST_REPAIR_CODES_API)
	public JsonArray getRepairCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_DAMAGE_CODES_API)
	public JsonArray getDamageCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_COMPONENT_CODES_API)
	public JsonArray getComponentCodes(@Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.LIST_OPERATORS_API)
	public JsonArray getOperators(@Query("modified_since") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonObject getContainerSessionsByPage(@Query("page") int page, @Query("modified_after") String lastModifiedDate);

	@GET(ApiEndpoint.CONTAINER_SESSIONS_API)
	public JsonObject searchContainer(@Query("keyword") String keyword);

	@GET(ApiEndpoint.CONTAINER_SESSION_ITEM_API)
	public Session getContainerSessionById(@Path("id") int containerId);

	@POST(ApiEndpoint.CONTAINER_SESSIONS_API)
	public void postContainer(@Field("username") String username, @Field("password") String password);

	// Check source v1, uploadType=media
	@POST(ApiEndpoint.CJAY_TMP_STORAGE)
	public void postImageFile(String uploadType, String name);
}