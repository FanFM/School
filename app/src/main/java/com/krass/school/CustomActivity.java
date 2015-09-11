
package com.krass.school;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Locale;

public abstract class CustomActivity extends AppCompatActivity {

	protected Toolbar toolbar;
	protected Dialog mDialog;

	private static final String TAG = "APILogs";

	// CY.SEND API object
	protected Communicator.ApiResult mApiResultHandler;
	protected Communicator mCommunicator;

	protected UserData mUserInformation;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());

		mUserInformation = UserData.getInstance(this);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}

		mApiResultHandler = cysendAPIHandler();
		if (mApiResultHandler != null) {
			initCYSENDAPI();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {

		Utils.d(this, "Pause activity: " + this.getClass().getSimpleName());
		super.onPause();
	}

	/**
	 * This method allow the abstract class to get the layout to use
	 *
	 * @return int
	 */
	protected abstract int getLayoutResourceId();

	/**
	 * This method allow the abstract class to return a CY.SEND API result handler If a handler is
	 * returned, the API will be initialized
	 *
	 * @return CYApiResult
	 */
	protected abstract Communicator.ApiResult cysendAPIHandler();

	private void initCYSENDAPI() {
		mCommunicator = new Communicator(this, mApiResultHandler, mUserInformation);

		mUserInformation.setParam(UserData.Params.LANGUAGE, getPhoneData());
	}

	public String getPhoneData() {
		// Get device information
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String language, device_sn = null, device_model, device_imei = null;
		String sim_number, sim_imsi, sim_country, sim_mccmnc, sim_operator, sim_serial = null;
		String network_country, network_mccmnc, network_operator = null;
		Integer network_type = 0;
		Boolean isinroaming = false;
		String os_version, cysend_android_package = null, cysend_android_version = null;

		language = Locale.getDefault().toString();
		device_model = Build.MANUFACTURER + " " + Build.MODEL;
		device_imei = telephonyManager.getDeviceId();
		if (device_imei == null)
			device_imei = "";
		sim_number = telephonyManager.getLine1Number();
		if (sim_number == null)
			sim_number = "";
		sim_imsi = telephonyManager.getSubscriberId();
		if (sim_imsi == null)
			sim_imsi = "";
		sim_country = telephonyManager.getSimCountryIso();
		try {
			URLDecoder.decode(sim_country, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (sim_country == null)
			sim_country = "";
		sim_mccmnc = telephonyManager.getSimOperator();
		if (sim_mccmnc == null)
			sim_mccmnc = "";
		sim_operator = telephonyManager.getSimOperatorName();
		try {
			URLDecoder.decode(sim_operator, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (sim_operator == null)
			sim_operator = "";
		sim_serial = telephonyManager.getSimSerialNumber();
		if (sim_serial == null)
			sim_serial = "";
		network_country = telephonyManager.getNetworkCountryIso();
		try {
			URLDecoder.decode(network_country, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (network_country == null)
			network_country = "";
		network_mccmnc = telephonyManager.getNetworkOperator();
		try {
			URLDecoder.decode(network_mccmnc, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (network_mccmnc == null)
			network_mccmnc = "";
		if (network_operator == null)
			network_operator = "";
		network_type = telephonyManager.getNetworkType();
		if (network_type == null)
			network_type = 0;
		isinroaming = telephonyManager.isNetworkRoaming();
		if (isinroaming == null)
			isinroaming = false;
		os_version = Build.VERSION.RELEASE;

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Integer density = metrics.densityDpi;

		// Insert current phone date/time
		Date current_date = new Date();
		CharSequence current_time = DateFormat
		.format("yyyy-MM-dd kk:mm:ss", current_date.getTime());

		try {
			cysend_android_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			cysend_android_package = getPackageName();
			// Test if it's a dev version
			if (cysend_android_version.contains("DEV")) {
				Utils.DEV_MODE = true;
				Utils.d(this, "!!!!!!!DEV MODE!!!!!!!!!!");
			}

			// Detect if application is running on an emulator
			if ("google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.MODEL)) {
				Utils.d(this, "!!!!!!!EMULATOR DETECT!!!!!!!!!!");
				if (!Utils.DEV_MODE) {
					// Block execution if not in dev mode
					mDialog = new Dialog(this);
					mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					mDialog.setCancelable(false);
					mDialog.setContentView(R.layout.material_error_dialog);
					TextView tvDialogTitle = (TextView) mDialog.findViewById(R.id.tvDialogTitle);
					TextView tvDialogMessage = (TextView) mDialog.findViewById(R.id.tvDialogMessage);
					Button btnAgree = (Button) mDialog.findViewById(R.id.btnAgree);
					tvDialogTitle.setText(R.string.emulator_forbiden_title);
					tvDialogMessage.setText(R.string.emulator_forbiden);
					btnAgree.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
							finish();
						}
					});
					mDialog.show();
					return "";
				}
			}

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			device_sn = (String) get.invoke(c, "ro.serialno");
		} catch (Exception ignored) {
		}
		if(language.contains("_")){
			return language.split("_")[0];
		}
		return language;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if((boolean)mUserInformation.getParam(UserData.Params.CONDITIONS) && !this.getClass().getSimpleName().equals("SettingsActivity") && !this.getClass().getSimpleName().equals("ContactUsActivity") && !this.getClass().getSimpleName().equals("ConditionsActivity")){
			getMenuInflater().inflate(R.menu.menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Intent intent = null;

		//noinspection SimplifiableIfStatement
		switch (item.getItemId()) {
			case R.id.action_settings:
				if (!this.getClass().getSimpleName().equals("SettingsActivity")) {
				}
				break;
			case android.R.id.home:
				finish();
			default:
				break;
		}
		if (intent != null) {
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}
}
