package com.krass.school;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by UADN_AK on 9/7/15.
 */
public class Utils {

	private static String URL ="http://www.google.com";


	public static Boolean DEV_MODE = true;
	public static Boolean TEST_MODE = false;
	public static Boolean JSONE_MODE = true;
	public static Boolean PRE_PROD_MODE = false;

	// Enum list of regular expressions
	public static enum Expressions {
		PHONE, VOUCHER, EMAIL, NAME, MESSAGE
	}

	private static Map<Expressions, String> regExpList = new HashMap<Expressions, String>() {

		/**
		 *
		 */
		private static final long serialVersionUID = 1854289539936574117L;

		{
			put(Expressions.PHONE, "^(\\+|00|011)?[\\d\\s]{7,20}[\\d]$");
			put(Expressions.VOUCHER, "[\\d]{6,50}");
			put(Expressions.EMAIL, "^[0-9a-zA-Z\\.\\-\\_]{1,}\\@[0-9a-zA-Z\\-\\_\\.]{1,}\\.[a-zA-Z]{2,256}$");
			put(Expressions.NAME,
			"^[0-9a-zA-ZÀÁÂÃÄÅÆÇĆÈÉÊËÌÍÎÏÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçćèéêëìíîïðñòóôõö÷øùúûüýþÿŒœŠšŸƒ\\-\\_\\.\\,\\s\\u0020\\u00C0-\\u00D6\\u00D9-\\u00DC\\u00E0-\\u00F6\\u00F9-\\u00FB\\u00D4\\u00DB]{2,50}$");
			put(Expressions.MESSAGE, "^[ \\n0-9a-zA-ZÀÁÂÃÄÅÆÇĆÈÉÊËÌÍÎÏÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçćèéêëìíîïðñòóôõö÷øùúûüýþÿŒœŠšŸƒ\\u0020-\\u002F\\u003A-\\u0040\\u005B-\\u0060\\u007B-\\u007E]{10,500}$");
		}
	};

	public static boolean regExp(String value, Expressions exp) {
		Pattern p;
		Matcher m;
		p = Pattern.compile(regExpList.get(exp));
		m = p.matcher(value);
		if (m.find()) {
			return true;
		}
		return false;

	}

	/**
	 * This method print a row in the log with an object name has tag
	 *
	 * @param o    an object to get the name of the tag
	 * @param text text to print in the log
	 */
	public static void d(Object o, String text) {

		// Use it only in dev mode
		if (!DEV_MODE && !PRE_PROD_MODE)
			return;

		int MAX_LENGTH = 470;
		String className;

		try {
			className = o.getClass().getSimpleName();
		} catch (Exception e) {
			className = "???";
		}

		int j = MAX_LENGTH, i = 0;
		while (i < text.length()) {
			if (j >= text.length()) {
				j = text.length();
			}
			// Log.d(className, text.substring(i, j));
			Log.d("APILogs", className + ": " + text.substring(i, j));
			i = j;
			j += MAX_LENGTH;
		}
	}

	public static void i(Object o, String text) {

		// Use it only in dev mode
		if (!DEV_MODE && !PRE_PROD_MODE)
			return;

		int MAX_LENGTH = 470;
		String className;

		try {
			className = o.getClass().getSimpleName();
		} catch (Exception e) {
			className = "???";
		}

		int j = MAX_LENGTH, i = 0;
		while (i < text.length()) {
			if (j >= text.length()) {
				j = text.length();
			}
			// Log.i(className, text.substring(i, j));
			Log.i("APILogs", className + ": " + text.substring(i, j));
			i = j;
			j += MAX_LENGTH;
		}
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String hex = Integer.toHexString(0xFF & aMessageDigest);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean checkInternetConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			if (activeNetwork.isConnectedOrConnecting()) {
				d(context, "Network status: active");
				return true;
			}
		}
		d(context, "Network status: not active");
		return false;
	}

	public static void makeScreen(Activity activity) {
		Bitmap bitmap;
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		FileOutputStream out = null;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath());
		dir.mkdirs();
		File file = new File(dir, "screen.png");

		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String createJsonRequest(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		result.append(URL).append("?");
		boolean first = true;
		for(Map.Entry<String, String> entry : params.entrySet()){
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}
