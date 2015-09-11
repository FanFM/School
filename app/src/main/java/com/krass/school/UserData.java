
package com.krass.school;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;

import java.util.HashMap;

public class UserData {

    public static ApiFunctions cashedFunction;
    public static HashMap<String, String> cashedParams;
    public static boolean renew_ticket = false;
    public static ProgressDialog pb_dialog;
    public static boolean check_echo = false;
    public static String vat = "";
    public static String ticket;
	private static final String USER_PREFS_NAME = "user_cash";
	private static UserData sInstance;
	private static SharedPreferences sSharedPreferences;

	/*
	 * Stored parameters `HashMap` of enum type `Params` and a `Pair` object has value. The `Pair`
	 * object will contain the unique string identifying the parameter has first element and another
	 * `Pair` has second element. The second `Pair` will contain 2 objects corresponding to the
	 * default value of the parameter for the first element and the current value of the parameter
	 * for the second element.
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<Params, Object> paramsValues;

	private UserData(Activity act) {
		sSharedPreferences = act.getSharedPreferences(USER_PREFS_NAME, 0);
		paramsValues = new HashMap<Params, Object>() {
			private static final long serialVersionUID = 3171697355333185172L;

			{
				put(Params.USER_FIRST_NAME, "");
				put(Params.USER_LAST_NAME, "");
				put(Params.USER_EMAIL, "");
				put(Params.USER_MOBILE, "");
				put(Params.LANGUAGE, "");
				put(Params.MOBILE, "");
				put(Params.CONDITIONS, false);
				put(Params.CARD_NUMBER, "");
				put(Params.MOBILE_NUMBER, "");
				put(Params.SMS_SCENARIO, false);
			}
		};
	}

	public static UserData getInstance(Activity act) {
		if (sInstance == null) {
			sInstance = new UserData(act);
		}
		return sInstance;
	}

	@SuppressWarnings("rawtypes")
	public Object getParam(Params parameter) {
		if(paramsValues.get(parameter) instanceof Boolean){
			return sSharedPreferences.getBoolean(parameter.name(), (Boolean) paramsValues.get(parameter));
		} else if(paramsValues.get(parameter) instanceof String){
			return sSharedPreferences.getString(parameter.name(), (String) paramsValues.get(parameter));
		} else if(paramsValues.get(parameter) instanceof Float){
			return sSharedPreferences.getFloat(parameter.name(), (Float) paramsValues.get(parameter));
		} else if(paramsValues.get(parameter) instanceof Integer){
			return sSharedPreferences.getInt(parameter.name(), (Integer) paramsValues.get(parameter));
		} else if(paramsValues.get(parameter) instanceof Long){
			return sSharedPreferences.getLong(parameter.name(), (Long) paramsValues.get(parameter));
		} else return null;
	}

	@SuppressWarnings({
	"rawtypes", "unchecked"
	})
	public void setParam(Params parameter, Object value) {

		SharedPreferences.Editor editor = sSharedPreferences.edit();
			if (value instanceof Boolean) {
				editor.putBoolean(parameter.name(), (Boolean) value);
				editor.apply();
			} else if (value instanceof String) {
				editor.putString(parameter.name(), (String)value);
				editor.apply();
			} else if (value instanceof Float) {
				editor.putFloat(parameter.name(), (Float) value);
				editor.apply();
			} else if (value instanceof Integer) {
				editor.putInt(parameter.name(), (int) value);
				editor.apply();
			} else if (value instanceof Long) {
				editor.putLong(parameter.name(), (Long) value);
				editor.apply();
			}
	}

	// Enum list of available parameters
	public static enum Params {
		USER_FIRST_NAME, USER_LAST_NAME, USER_EMAIL, USER_MOBILE, LANGUAGE, MOBILE, CONDITIONS, CARD_NUMBER, MOBILE_NUMBER, SMS_SCENARIO
	}

}
