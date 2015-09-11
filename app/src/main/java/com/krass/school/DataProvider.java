package com.krass.school;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by UADN_AK on 7/3/15.
 */
public class DataProvider {

	private final String PREFS_NAME = "functions_cash";
	private SharedPreferences sSharedPreferences;

	private static DataProvider sInstance = null;

	private DataProvider(Context context){
		sSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public static DataProvider getInstance(Context context){
		if(sInstance == null)
		{
			sInstance = new DataProvider(context);
		}
		return sInstance;
	}

	public Boolean setData(ApiFunctions apiFunctions, String params) {

		SharedPreferences.Editor editor = sSharedPreferences.edit();
		editor.putString(apiFunctions.name(), params);
		editor.apply();
		return true;

	}

	public String getData(ApiFunctions apiFunctions) {
		return sSharedPreferences.getString(apiFunctions.name(), "");
	}

}
