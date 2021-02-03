package com.brightpattern.api.utils;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONUtils {

    public static String getString(JsonObject obj, String field) {
        JsonElement value = obj.get(field);
        return value==null?null:value.getAsString();
    }
}
