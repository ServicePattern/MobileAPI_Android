package com.brightpattern.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonArrayResponseHandler extends BaseStringResponseHandler<List<JsonObject>> {
	
	private String arrayField;

	public JsonArrayResponseHandler(String arrayField) {
		this.arrayField = arrayField;
	}

	@Override
	public List<JsonObject> handleSuccessResponse(HttpResponse response, String body) throws IOException {
		JsonParser parser = new JsonParser();
		List<JsonObject> result = new ArrayList<JsonObject>();
		if (body != null && !body.isEmpty()) {
			JsonElement elem = parser.parse(body);
			if (arrayField != null) {
				elem = elem.getAsJsonObject().get(arrayField);
			}
			JsonArray array = elem.getAsJsonArray();

			for (int i = 0; i < array.size(); i++) {
				result.add(array.get(i).getAsJsonObject());
			}
		}
		return result;
	}
}