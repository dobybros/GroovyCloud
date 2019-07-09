package com.docker.utils;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class JsonObjectEx {
	private JsonObject jsonObject;
	
	public JsonObjectEx(String json) {
		JsonElement element = new JsonParser().parse(json);
		if(element != null) {
			jsonObject = element.getAsJsonObject();
		}
	}
	
	public JsonObjectEx(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	
	public JsonObjectEx() {
		this.jsonObject = new JsonObject();
	}
	
	@Override
	public String toString() {
		if(jsonObject == null)
			return "no json";
		return jsonObject.toString();
	}
	
	public Long getLong(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if(primitive.isNumber()) 
				return primitive.getAsLong();
		}
		return null;
	}
	
	public void writeLong(String key, Long value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			jsonObject.addProperty(key, value);
		}
	}
	
	public String getString(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if(primitive.isString()) 
				return primitive.getAsString();
		}
		return null;
	}
	
	public void writeString(String key, String value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			jsonObject.addProperty(key, value);
		}
	}
	
	public Integer getInteger(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if(primitive.isNumber()) 
				return primitive.getAsInt();
		}
		return null;
	}
	
	public void writeInteger(String key, Integer value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			jsonObject.addProperty(key, value);
		}
	}
	
	public JsonObjectEx getJsonObjectEx(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonObject()) {
			JsonObjectEx joe = new JsonObjectEx(element.getAsJsonObject());
			return joe;
		}
		return null;
	}
	
	public void writeJsonObjectEx(String key, JsonObjectEx value) {
		if(jsonObject == null)
			return;
		if(value != null && value.jsonObject != null) {
			jsonObject.add(key, value.jsonObject);
		}
	}
	
	public JsonArray getJsonArray(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonArray()) {
			return element.getAsJsonArray();
		}
		return null;
	}
	
	public void writeJsonArray(String key, JsonArray value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			jsonObject.add(key, value);
		}
	}
	
	public List<String> getStringArray(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			List<String> stringList = new ArrayList<String>();
			for(int i = 0; i < array.size(); i++) {
				JsonElement item = array.get(i);
				if(item != null && item.isJsonPrimitive()) {
					String str = item.getAsString();
					stringList.add(str);
				}
			}
			return stringList;
		}
		return null;
	}
	
	public void writeStringArray(String key, List<String> value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			JsonArray array = new JsonArray();
			for(String v : value) {
				array.add(v);
			}
			jsonObject.add(key, array);
		}
	}
	
	public Boolean getBoolean(String key) {
		if(jsonObject == null)
			return null;
		JsonElement element = jsonObject.get(key);
		if(element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if(primitive.isBoolean()) 
				return primitive.getAsBoolean();
		}
		return null;
	}
	
	public void writeBoolean(String key, Boolean value) {
		if(jsonObject == null)
			return;
		if(value != null) {
			jsonObject.addProperty(key, value);
		}
	}
}
