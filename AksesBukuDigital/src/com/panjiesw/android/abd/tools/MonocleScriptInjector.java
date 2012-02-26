package com.panjiesw.android.abd.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MonocleScriptInjector {
	public static String[] getJavascript(String[] path, String[] titles, String[] metas) {
		
		JSONArray titleJson = new JSONArray();
		for (int i = 0; i < path.length; i++) {
			titleJson.put("file:///data/data/com.panjiesw.android.abd/app_epubtemp/component/"+path[i]);
		}
		
		JSONArray contentsJson = new JSONArray();
		for (int i = 0; i < titles.length; i++) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("title", titles[i]);
				obj.put("src", "file:///data/data/com.panjiesw.android.abd/app_epubtemp/component/"+path[i]);
				contentsJson.put(obj);
			} catch (JSONException e) {
				Log.e(Misc.TAG_E, e.getMessage());
			}
		}
		
		JSONObject metasJson = new JSONObject();
		for (int i = 0; i < metas.length; i++) {
			try {
				metasJson.put("title", metas[1]);
				metasJson.put("creator", metas[0]);
			} catch (JSONException e) {
				Log.e(Misc.TAG_E, e.getMessage());
			}
		}
		
		Log.d(MonocleScriptInjector.class.toString(), titleJson.toString());
		Log.d(MonocleScriptInjector.class.toString(), contentsJson.toString());
		Log.d(MonocleScriptInjector.class.toString(), metasJson.toString());
		
		String[] bookData = {titleJson.toString(),contentsJson.toString(),metasJson.toString()};
		
		return bookData;
	}
}
