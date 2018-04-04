package com.xinrui.smart.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListDataSave {
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	public ListDataSave(Context mContext, String preferenceName) {
		preferences = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	/**
	 * 保存List
	 * @param tag
	 * @param datalist
	 */
	public <T> void setDataList(String tag, List<T> datalist) {
		if (null == datalist || datalist.size() <= 0)
			return;

		Gson gson = new GsonBuilder().
				registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

					@Override
					public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
						if (src == src.longValue())
							return new JsonPrimitive(src.longValue());
						return new JsonPrimitive(src);
					}
				}).create();
		//转换成json数据，再保存
		String strJson = gson.toJson(datalist);

		editor.putString(tag, strJson);
		editor.commit();

	}

	/**
	 * 获取lIst
	 * @param tag
	 * @return
	 */
	public <T> List<T> getDataList(String tag) {
		List<T> datalist=new ArrayList<T>();
		String strJson = preferences.getString(tag, null);
		if (null == strJson) {
			return datalist;
		}
		Gson gson = new GsonBuilder().
				registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

					@Override
					public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
						if (src == src.longValue())
							return new JsonPrimitive(src.longValue());
						return new JsonPrimitive(src);
					}
				}).create();
		datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
		}.getType());
		return datalist;
	}
}