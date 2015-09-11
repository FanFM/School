package com.krass.school;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Communicator {


	// Secret pass phrase used to calculate the hash code of API requests
	@SuppressWarnings("FieldCanBeLocal")
	private static String API_SECRET = "7e779394647eb737bc3fd5eb4e1d320c";

	// Private variable to store current activity in order to interact with it
	private CustomActivity mCustomActivity;
	private UserData mUserData;
	private Dialog mDialog;
	// Private variable to store CY.SEND API Async Task.

	// Private variable to store the API result interface given in parameter to
	// the constructor
	private ApiResult mApiResult;

	// List of function that can be cachable
	private static List<ApiFunctions> cachableFunctions = null;

	/*
	 * Stored parameters `HashMap` of enum type `ApiFunctions` and a `Pair` object has value. The `Pair` object will
	 * contain the unique string identifying the function has first element and another `HashMap` has second element.
	 * The second `HashMap` will contain the list of function parameters with parameter name as key and if the
	 * parameters is mandatory has value (Boolean)
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<ApiFunctions, Pair> sFunctionParams = null;

	/**
	 * This is a class to execute requests in background
	 */
	private class ApiRunner extends AsyncTask<Void, Void, String> {

		private ApiFunctions executedFunction;
		//		private List<NameValuePair> executed_function_params;
		private HashMap<String, String> hmFunctionParams;
		private long requestTime;

		/**
		 * This method create the object to execute in background the method passed in parameter.
		 *
		 * @param function the function to execute
		 * @param hmParams a list of parameters for the function
		 */
		ApiRunner(ApiFunctions function, HashMap<String, String> hmParams) {

			// Store the current time
			requestTime = System.currentTimeMillis();
			executedFunction = function;
			hmFunctionParams = hmParams;
		}

		@Override
		protected String doInBackground(Void... params) {

			// Execute the request
			try {
				return httpsPost(hmFunctionParams);
			} catch (IOException e) {
				e.printStackTrace();
				return "-1";
			}
		}

		@Override
		protected void onPostExecute(String response) {
			super.onPostExecute(response);
			Utils.d(this, "REQUEST TIME: " + (System.currentTimeMillis() - requestTime) + " msec");
			// Proceed to callback
			resultSerializer(executedFunction, response);
		}

	}

	/**
	 * Interface class to manage API results
	 */
	public interface ApiResult {

		/**
		 * This method is called when a response from CY.SEND API arrived.
		 *
		 * @param response the response
		 */
		void onResponse(ApiFunctions function, Object response);

		/**
		 * This method is called when an activity_error is received from a CY.SEND API request. The method can manage the activity_error
		 * or return false so the generic activity_error message will be displayed.
		 *
		 * @param method   the method executed
		 * @param response the response
		 * @return Boolean
		 */
		Boolean errorManager(ApiFunctions method, Object response, String error_code, String error_msg);
	}

	/**
	 * Constructor of the API object
	 *
	 * @param activity  allow the API to interact with the current activity
	 * @param apiResult allow the API to execute activity result methods
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Communicator(CustomActivity activity, ApiResult apiResult, UserData userData) {
		mCustomActivity = activity;
		mApiResult = apiResult;
		mUserData = userData;
		if (sFunctionParams == null) {
			sFunctionParams = new HashMap<ApiFunctions, Pair>() {

				/**
				 *
				 */
				private static final long serialVersionUID = 10L;

				{
					put(ApiFunctions.GET_TICKET, new Pair("get_ticket", new HashMap<String, Boolean>() {

						/**
						 *
						 */
						private static final long serialVersionUID = 20L;

						{
							put("time", true);
						}
					}));
					put(ApiFunctions.LOGIN, new Pair("login", new HashMap<String, Boolean>() {

						/**
						 *
						 */
						private static final long serialVersionUID = 30L;

						{
							put("login", true);
							put("password", true);
						}
					}));
				}
			};
		}
		if (cachableFunctions == null) {
			cachableFunctions = new ArrayList<>();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Boolean sendRequest(ApiFunctions function, HashMap<String, String> params) {

		HashMap<String, String> paramsRequest = new HashMap<>();

		// Retrieve function definition
		Pair pair_obj = sFunctionParams.get(function);
		// If function don't exist
		if (pair_obj == null)
			return false;

		paramsRequest.put("function", function.name().toLowerCase());

		HashMap<String, Boolean> functionParamsList = (HashMap<String, Boolean>) pair_obj.second;

		for (HashMap.Entry<String, Boolean> parameter : functionParamsList.entrySet()) {
			// Retrieve corresponding parameter in the new request data
			String value = params.get(parameter.getKey());
			Utils.d(mCustomActivity, "Testing parameter \"" + parameter.getKey() + "\" value: " + value);
			// If the parameter is mandatory and it's not present in the new
			// request data
			if (parameter.getValue() && value == null)
				return false;
			// If new request data is present: add it in the sending list
			if (value != null)
				paramsRequest.put(parameter.getKey(), value);
		}
		// Calculate the hash code
		String hash = calculateHash(paramsRequest);
		paramsRequest.put("hash", hash);
		// last_ticket_params = params;
		// Execute the query
		ApiRunner api_task = new ApiRunner(function, paramsRequest);
		api_task.execute();
		return true;
	}

	private String calculateHash(HashMap<String, String> hmParams) {
		Collection<String> cParams = hmParams.values();
		String key = "";
		// default keys
		for (String param : cParams) {
			key = key + param + "|";
		}
		key += API_SECRET;
		System.getProperty("line.separator");
		key = key.replace("\n", "").replace("\r", "");
		String hash = Utils.md5(key);
		Utils.d(mCustomActivity, "hash: " + key + " = " + hash);
		return hash;
	}


	private String httpsPost(HashMap<String, String> hmParams) throws IOException {

		URL urlToRequest;
		String response = "";
		Utils.d(mCustomActivity, "URL: " + Consts.API_URL);
		urlToRequest = new URL(Consts.API_URL);
		HttpURLConnection urlConnection = (HttpURLConnection) urlToRequest.openConnection();
		int timeoutConnection = 20000;
		int timeoutSocket = 20000;
		urlConnection.setConnectTimeout(timeoutConnection);
		urlConnection.setReadTimeout(timeoutSocket);

		String postParameters = createJsonRequest(hmParams);
		Utils.d(mCustomActivity, "Request: " + postParameters);

		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		urlConnection.setRequestMethod("POST");
		urlConnection.setFixedLengthStreamingMode(
		postParameters.getBytes().length);
		urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


		OutputStream os = urlConnection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(
		new OutputStreamWriter(os, "UTF-8"));
		writer.write(postParameters);

		writer.flush();
		writer.close();
		os.close();

		int responseCode = urlConnection.getResponseCode();

		if (responseCode == HttpsURLConnection.HTTP_OK) {
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while ((line = br.readLine()) != null) {
				response += line;
			}
		} else {
			response = "-1";
		}

		return response;
	}

	public static String createJsonRequest(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
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

	/**
	 * This method serialize a response and call the callback method.
	 *
	 * @param method   the method executed
	 * @param response the response to this method
	 */
	private void resultSerializer(final ApiFunctions method, String response) {

		// store response in DataProvider
		DataProvider.getInstance(mCustomActivity).setData(method, response);

		Utils.d(this, "resultSerializer method: " + method.toString() + " response: " + response);
		// If the response is incorrect
		if (response.equals("-1")) {
			// Connection problem
			if (!mApiResult.errorManager(method, response, "-1", "Incorrect response")) {
				showError(method, response);
			}
			return;
		}
		try {
			switch (method) {
				case GET_TICKET:
					GetTicketResponse getTicketResponse = (GetTicketResponse) JsonParser.getInstance(mCustomActivity).restoreObject(method);
					if (getTicketResponse.error.equals("0")) {
						mApiResult.onResponse(method, getTicketResponse);
					} else {
						if (!mApiResult.errorManager(method, response, getTicketResponse.error, getTicketResponse.error_text)) {
							showError(method, response);
						}
					}
					break;
				case LOGIN:
					LoginResponse loginResponse = (LoginResponse) JsonParser.getInstance(mCustomActivity).restoreObject(method);
					if (loginResponse.error.equals("0")) {
						mApiResult.onResponse(method, loginResponse);
					} else {
						if (!mApiResult.errorManager(method, response, loginResponse.error, loginResponse.error_text)) {
							showError(method, response);
						}
					}
					break;
				default:
					if (!mApiResult.errorManager(method, response, "-2", "Unknown function")) {
						showError(method, response);
					}
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (!mApiResult.errorManager(method, response, "-3", "Response parssing exception")) {
				showError(method, response);
			}
		}
	}

	private void showError(ApiFunctions method, String response) {

		try {
			if (response.equals("-1")) {
				mDialog = new Dialog(mCustomActivity);
				mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				mDialog.setCancelable(false);
				mDialog.setContentView(R.layout.material_error_dialog);
				TextView tvDialogTitle = (TextView) mDialog.findViewById(R.id.tvDialogTitle);
				TextView tvDialogMessage = (TextView) mDialog.findViewById(R.id.tvDialogMessage);
				Button btnAgree = (Button) mDialog.findViewById(R.id.btnAgree);
				tvDialogTitle.setText(mCustomActivity.getString(R.string.cysendapiunavailable_title));
				tvDialogMessage.setText(mCustomActivity.getString(R.string.cysendapiunavailable));
				btnAgree.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						Intent intent = new Intent(mCustomActivity, LoginActivity.class);
						mCustomActivity.startActivity(intent);
						mCustomActivity.finish();
					}
				});
				mDialog.show();
			} else {
				Gson gson = new Gson();
				StandardApiResponse standart_response = gson.fromJson(response, StandardApiResponse.class);
				String error = standart_response.error;
				String error_text = standart_response.error_text;
				Utils.d(mCustomActivity, "Error: " + error + " Text: " + error_text);
				if (error == null) {
					goToErrorActivity();
				} else {
					Resources res = mCustomActivity.getResources();
					String[] errors_cat = null;
					// Retrieve activity_error code translation
					if (error.startsWith("S")) {
						errors_cat = res.getStringArray(R.array.errors_cat_S);
					} else if (error.startsWith("P")) {
						errors_cat = res.getStringArray(R.array.errors_cat_P);
					} else if (error.startsWith("X")) {
						errors_cat = res.getStringArray(R.array.errors_cat_X);
					}
					if (errors_cat == null) {
						goToErrorActivity();
					} else {
						String translated_error = "";
						for (String error_elem : errors_cat) {
							String app_error = error_elem.split("[|]")[0];
							if (app_error.equals(error)) {
								translated_error = error_elem;
							}
						}
						String[] errors = translated_error.split("[|]");
						if (errors.length > 0) {
							String final_error = errors[1];
							final String restart_error = errors[2];
							String text_error = errors[3];
							String text_error_title = res.getString(R.string.error_default_title);
							String text_error_button = res.getString(R.string.error_default_button);
							if (final_error.equals("1")) {
								goToErrorActivity();
							} else {
								mDialog = new Dialog(mCustomActivity);
								mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
								mDialog.setCancelable(false);
								mDialog.setContentView(R.layout.material_error_dialog);
								TextView tvDialogTitle = (TextView) mDialog.findViewById(R.id.tvDialogTitle);
								TextView tvDialogMessage = (TextView) mDialog.findViewById(R.id.tvDialogMessage);
								Button btnAgree = (Button) mDialog.findViewById(R.id.btnAgree);
								tvDialogTitle.setText(text_error_title);
								tvDialogMessage.setText(text_error);
								btnAgree.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										mDialog.dismiss();
										if (restart_error.equals("1")) {
											Intent iEnterCardNumber = new Intent(mCustomActivity, LoginActivity.class);
											mCustomActivity.startActivity(iEnterCardNumber);
											mCustomActivity.finish();
										}
									}
								});
								mDialog.show();
							}
						} else {
							goToErrorActivity();
						}
					}
				}
			}
		} catch (Exception e) {
			goToErrorActivity();
		}
	}

	private void goToErrorActivity() {
		mDialog = new Dialog(mCustomActivity);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCancelable(false);
		mDialog.setContentView(R.layout.material_error_dialog);
		TextView tvDialogTitle = (TextView) mDialog.findViewById(R.id.tvDialogTitle);
		TextView tvDialogMessage = (TextView) mDialog.findViewById(R.id.tvDialogMessage);
		Button btnAgree = (Button) mDialog.findViewById(R.id.btnAgree);
		tvDialogTitle.setText(mCustomActivity.getString(R.string.general_api_problem_title));
		tvDialogMessage.setText(mCustomActivity.getString(R.string.general_api_problem));
		btnAgree.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				Intent intent = new Intent(mCustomActivity, LoginActivity.class);
				mCustomActivity.startActivity(intent);
				mCustomActivity.finish();
			}
		});
		mDialog.show();
	}
}
