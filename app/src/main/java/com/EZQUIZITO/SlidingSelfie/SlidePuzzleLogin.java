package com.EZQUIZITO.SlidingSelfie;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

public class SlidePuzzleLogin extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    final private String TAG = "_DEBUG";

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + resultCode + ", intent=" + data);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_puzzle_login);

        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();

        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);
    }

    /** Called when the "sign in" or "sign out" button is clicked. */
    @Override
    public void onClick(View view) {
        Log.i(TAG, "Entrou no onClick!");
        switch (view.getId()) {
            case R.id.button_sign_in:
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                //if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // sign out.
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                showSignInBar();
                break;

        }
    }

    private boolean isConnected() {
        return mGoogleApiClient == null || !mGoogleApiClient.isConnected();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Sign-in worked!
        log("Sign-in successful! Loading game state from cloud.");
        showSignOutBar();
        Intent i = new Intent(this, SlidePuzzleMain.class);
        startActivity(i);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended() called. Cause: " + cause);
        // onConnected will automatically be called when the client reconnects.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils
                    .resolveConnectionFailure(this, mGoogleApiClient,
                            connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }
        showSignInBar();
    }

    /** Prints a log message (convenience method). */
    void log(String message) {
        Log.d(TAG, message);
    }

    /** Shows the "sign in" bar (explanation and button). */
    private void showSignInBar() {
        findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
    }

    /** Shows the "sign out" bar (explanation and button). */
    private void showSignOutBar() {
        findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
        findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
    }
}
