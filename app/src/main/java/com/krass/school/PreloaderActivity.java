package com.krass.school;

import android.content.Intent;
import android.os.Bundle;

import com.krass.school.util.SystemUiHider;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class PreloaderActivity extends CustomActivity implements Communicator.ApiResult {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTicket();
	}

	@Override
	protected int getLayoutResourceId() {
		return R.layout.activity_preloader;
	}

	@Override
	protected Communicator.ApiResult cysendAPIHandler() {
		return this;
	}

	private void getTicket() {
		HashMap<String, String> params = new HashMap<>();
		params.put("time", "17:26");
		mCommunicator.sendRequest(ApiFunctions.GET_TICKET, params);
	}

	@Override
	public void onResponse(ApiFunctions function, Object response) {
		if (function.equals(ApiFunctions.GET_TICKET)) {
			GetTicketResponse mGetTicketResponse = (GetTicketResponse) response;
			Utils.d(PreloaderActivity.this, mGetTicketResponse.ticket);
			startActivity(new Intent(PreloaderActivity.this, LoginActivity.class));
		}
	}

	@Override
	public Boolean errorManager(ApiFunctions method, Object response, String error_code, String error_msg) {
		return true;
	}
}
