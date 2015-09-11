package com.krass.school;

import android.content.Context;

import com.google.gson.Gson;

/**
 * Created by UADN_AK on 7/3/15.
 */
public class JsonParser {

	private static JsonParser sInstance = null;

	private Gson gson;
	private Context mContext;

	private JsonParser(Context context) {
		mContext = context;
		gson = new Gson();
	}

	public static JsonParser getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new JsonParser(context);
		}
		return sInstance;
	}

	public Object restoreObject(final ApiFunctions method) {
		try {
			switch (method) {
				case GET_TICKET:
					return gson.fromJson(DataProvider.getInstance(mContext).getData(method), GetTicketResponse.class);
				case LOGIN:
					return gson.fromJson(DataProvider.getInstance(mContext).getData(method), LoginResponse.class);
				default:
					return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void storeObject(final ApiFunctions method, Object o) {
		if(o == null) {
			DataProvider.getInstance(mContext).setData(method, "");
		} else {
			DataProvider.getInstance(mContext).setData(method, gson.toJson(o));
		}
	}
}
