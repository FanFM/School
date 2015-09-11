package com.krass.school;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.util.HashMap;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends CustomActivity implements
View.OnClickListener,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, Communicator.ApiResult {

	private static final String TAG = "MainActivity";

	/* RequestCode for resolutions involving sign-in */
	private static final int RC_SIGN_IN = 9001;

	/* Keys for persisting instance variables in savedInstanceState */
	private static final String KEY_IS_RESOLVING = "is_resolving";
	private static final String KEY_SHOULD_RESOLVE = "should_resolve";

	/* Client for accessing Google APIs */
	private GoogleApiClient mGoogleApiClient;

	/* View to display current status (signed-in, signed-out, disconnected, etc) */
	private TextView mStatus;

	/* Is there a ConnectionResult resolution in progress? */
	private boolean mIsResolving = false;

	/* Should we automatically resolve ConnectionResults when possible? */
	private boolean mShouldResolve = false;

	private View mProgress;
	private RelativeLayout rlTop, rlBottom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Restore from saved instance state
		// [START restore_saved_instance_state]
		if (savedInstanceState != null) {
			mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
			mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
		}
		// [END restore_saved_instance_state]

		// Set up button click listeners
		findViewById(R.id.sign_in_button).setOnClickListener(this);
//		findViewById(R.id.sign_out_button).setOnClickListener(this);
//		findViewById(R.id.disconnect_button).setOnClickListener(this);
		mProgress = findViewById(R.id.pbProgress);
		rlTop = (RelativeLayout) findViewById(R.id.rlTop);
		rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);

		// Large sign-in
		((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);

		// Start with sign-in button disabled until sign-in either succeeds or fails
		findViewById(R.id.sign_in_button).setEnabled(false);

		// Set up view instances
		mStatus = (TextView) findViewById(R.id.tvStatus);

		// [START create_google_api_client]
		// Build GoogleApiClient with access to basic profile
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Plus.API)
		.addScope(new Scope(Scopes.PROFILE))
		.build();

		// [END create_google_api_client]

	}

	private void checkAccount() {
		showProgress(true);
		GetTicketResponse mGetTicketResponse = (GetTicketResponse) JsonParser.getInstance(this).restoreObject(ApiFunctions.GET_TICKET);
		HashMap<String, String> params = new HashMap<>();
		params.put("login", Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName());
		params.put("password", Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getId());
		params.put("ticket", mGetTicketResponse.ticket);
		mCommunicator.sendRequest(ApiFunctions.LOGIN, params);
	}

	private void updateUI(boolean isSignedIn) {
		if (isSignedIn) {
			// Show signed-in user's name
			String name = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName();
			mStatus.setText(getString(R.string.signed_in_fmt, name));

			// Set button visibility
			findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//			findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
			checkAccount();

		} else {
			// Show signed-out message
			mStatus.setText(R.string.signed_out);

			// Set button visibility
			findViewById(R.id.sign_in_button).setEnabled(true);
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//			findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
		}
	}

	// [START on_start_on_stop]
	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	@Override
	protected int getLayoutResourceId() {
		return R.layout.activity_login;
	}

	@Override
	public Communicator.ApiResult cysendAPIHandler() {
		return this;
	}
	// [END on_start_on_stop]

	// [START on_save_instance_state]
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
		outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
	}
	// [END on_save_instance_state]

	// [START on_activity_result]
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

		if (requestCode == RC_SIGN_IN) {
			// If the error resolution was not successful we should not resolve further errors.
			if (resultCode != RESULT_OK) {
				mShouldResolve = false;
			}

			mIsResolving = false;
			mGoogleApiClient.connect();
		}
	}
	// [END on_activity_result]

	@Override
	public void onConnected(Bundle bundle) {
		// onConnected indicates that an account was selected on the device, that the selected
		// account has granted any requested permissions to our app and that we were able to
		// establish a service connection to Google Play services.
//		Log.d(TAG, "onConnected:" + bundle);

		// Show the signed-in UI
		updateUI(true);
	}

	@Override
	public void onConnectionSuspended(int i) {
		// The connection to Google Play services was lost. The GoogleApiClient will automatically
		// attempt to re-connect. Any UI elements that depend on connection to Google APIs should
		// be hidden or disabled until onConnected is called again.
		Log.w(TAG, "onConnectionSuspended:" + i);
	}

	// [START on_connection_failed]
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Could not connect to Google Play Services.  The user needs to select an account,
		// grant permissions or resolve an error in order to sign in. Refer to the javadoc for
		// ConnectionResult to see possible error codes.
		Log.d(TAG, "onConnectionFailed:" + connectionResult);

		if (!mIsResolving && mShouldResolve) {
			if (connectionResult.hasResolution()) {
				try {
					connectionResult.startResolutionForResult(this, RC_SIGN_IN);
					mIsResolving = true;
				} catch (IntentSender.SendIntentException e) {
					Log.e(TAG, "Could not resolve ConnectionResult.", e);
					mIsResolving = false;
					mGoogleApiClient.connect();
				}
			} else {
				// Could not resolve the connection result, show the user an
				// error dialog.
				showErrorDialog(connectionResult);
			}
		} else {
			// Show the signed-out UI
			updateUI(false);
		}
	}
	// [END on_connection_failed]

	private void showErrorDialog(ConnectionResult connectionResult) {
		int errorCode = connectionResult.getErrorCode();

		if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
			// Show the default Google Play services error dialog which may still start an intent
			// on our behalf if the user can resolve the issue.
			GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
			new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mShouldResolve = false;
					updateUI(false);
				}
			}).show();
		} else {
			// No default Google Play Services error, display a message to the user.
			String errorString = getString(R.string.play_services_error_fmt, errorCode);
			Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

			mShouldResolve = false;
			updateUI(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_in_button:
				// User clicked the sign-in button, so begin the sign-in process and automatically
				// attempt to resolve any errors that occur.
				mStatus.setText(R.string.signing_in);
				// [START sign_in_clicked]
				mShouldResolve = true;
				mGoogleApiClient.connect();
				// [END sign_in_clicked]
				break;
//			case R.id.sign_out_button:
//				// Clear the default account so that GoogleApiClient will not automatically
//				// connect in the future.
//				// [START sign_out_clicked]
//				if (mGoogleApiClient.isConnected()) {
//					Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//					mGoogleApiClient.disconnect();
//				}
//				// [END sign_out_clicked]
//				updateUI(false);
//				break;
//			case R.id.disconnect_button:
//				// Revoke all granted permissions and clear the default account.  The user will have
//				// to pass the consent screen to sign in again.
//				// [START disconnect_clicked]
//				if (mGoogleApiClient.isConnected()) {
//					Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//					Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
//					mGoogleApiClient.disconnect();
//				}
//				// [END disconnect_clicked]
//				updateUI(false);
//				break;
		}
	}

	private void showProgress(final boolean show) {
		rlTop.setVisibility(show ? View.GONE : View.VISIBLE);
		rlBottom.setVisibility(show ? View.GONE : View.VISIBLE);
		mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onResponse(ApiFunctions function, Object response) {

		if (function.equals(ApiFunctions.LOGIN)) {
			LoginResponse loginResponse = (LoginResponse) response;
			Utils.d(LoginActivity.this, loginResponse.status);
			showProgress(false);
			startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
		}
	}

	@Override
	public Boolean errorManager(ApiFunctions method, Object response, String error_code, String error_msg) {
		return false;
	}
}
