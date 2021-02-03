package com.brightpattern.api.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class JsonResponseHandler<T> extends BaseStringResponseHandler<T> {

	private Class<T> type;

	public JsonResponseHandler(Class<T> type) {
		this.type = type;
	}
	
	@Override
	public T handleSuccessResponse(HttpResponse response, String body) throws IOException {
		return new Gson().fromJson(body, type);
	}
}
