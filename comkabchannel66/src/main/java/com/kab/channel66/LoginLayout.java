package com.kab.channel66;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.browser.customtabs.CustomTabsIntent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kab.channel66.auth.AuthStateManager;
import com.kab.channel66.auth.Configuration;
import com.kab.channel66.auth.LoginActivity;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.RegistrationRequest;
import net.openid.appauth.RegistrationResponse;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.browser.AnyBrowserMatcher;
import net.openid.appauth.browser.BrowserMatcher;
import net.openid.appauth.browser.ExactBrowserMatcher;

public class LoginLayout extends Activity {
    EditText un,pw;
	TextView error;
    Button ok;
    LoginButton loginButton;
    CallbackManager callbackManager;

    private static final String TAG = "LoginActivity";



    ////keycloak

    private static final String EXTRA_FAILED = "failed";
    private static final int RC_AUTH = 100;

    private AuthorizationService mAuthService;
    private AuthStateManager mAuthStateManager;
    private Configuration mConfiguration;

    private final AtomicReference<String> mClientId = new AtomicReference<>();
    private final AtomicReference<AuthorizationRequest> mAuthRequest = new AtomicReference<>();
    private final AtomicReference<CustomTabsIntent> mAuthIntent = new AtomicReference<>();
    private CountDownLatch mAuthIntentLatch = new CountDownLatch(1);
    private ExecutorService mExecutor;

    private boolean mUsePendingIntents;

    @NonNull
    private BrowserMatcher mBrowserMatcher = AnyBrowserMatcher.INSTANCE;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        un=(EditText)findViewById(R.id.et_un);
        pw=(EditText)findViewById(R.id.et_pw);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.tv_error);

        mExecutor = Executors.newSingleThreadExecutor();
        mAuthStateManager = AuthStateManager.getInstance(this);
        mConfiguration = Configuration.getInstance(this);

        if (mAuthStateManager.getCurrent().isAuthorized()
                && !mConfiguration.hasConfigurationChanged()) {
            Log.i(TAG, "User is already authenticated, proceeding to token activity");
//            startActivity(new Intent(this, TokenActivity.class));
            finish();
            return;
        }


        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//
//            	ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
//            	postParameters.add(new BasicNameValuePair("username", un.getText().toString()));
//            	postParameters.add(new BasicNameValuePair("password", pw.getText().toString()));
//
//            	String response = null;
//            	try {
//            	    response = CustomHttpClient.executeHttpPost("<target page url>", postParameters);
//            	    String res=response.toString();
//            	    res= res.replaceAll("\\s+","");
//            	    if(res.equals("1"))
//            	    	error.setText("Correct Username or Password");
//            	    else
//            	    	error.setText("Sorry!! Incorrect Username or Password");
//            	} catch (Exception e) {
//            		un.setText(e.toString());
//            	}
                startAuth();

            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setLoginBehavior(LoginBehavior.DEVICE_AUTH);
        callbackManager =  CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("login","success");
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("login","cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("login","error");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //displayAuthOptions();
        if (resultCode == RESULT_CANCELED) {
            displayAuthCancelled();
        } else {
//            Intent intent = new Intent(this, TokenActivity.class);
//            intent.putExtras(data.getExtras());
//            startActivity(intent);

        }
    }

    @MainThread
    void startAuth() {
        displayLoading("Making authorization request");

       // mUsePendingIntents = ((CheckBox) findViewById(R.id.pending_intents_checkbox)).isChecked();

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        mExecutor.submit(this::doAuth);
    }

    /**
     * Initializes the authorization service configuration if necessary, either from the local
     * static values or by retrieving an OpenID discovery document.
     */
    @WorkerThread
    private void initializeAppAuth() {
        Log.i(TAG, "Initializing AppAuth");
        recreateAuthorizationService();

        if (mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration() != null) {
            // configuration is already created, skip to client initialization
            Log.i(TAG, "auth config already established");
            initializeClient();
            return;
        }

        // if we are not using discovery, build the authorization service configuration directly
        // from the static configuration values.
        if (mConfiguration.getDiscoveryUri() == null) {
            Log.i(TAG, "Creating auth config from res/raw/auth_config.json");
            AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                    mConfiguration.getAuthEndpointUri(),
                    mConfiguration.getTokenEndpointUri(),
                    mConfiguration.getRegistrationEndpointUri(),
                    mConfiguration.getEndSessionEndpoint());

            mAuthStateManager.replace(new AuthState(config));
            initializeClient();
            return;
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        runOnUiThread(() -> displayLoading("Retrieving discovery document"));
        Log.i(TAG, "Retrieving OpenID discovery doc");
        AuthorizationServiceConfiguration.fetchFromUrl(
                mConfiguration.getDiscoveryUri(),
                this::handleConfigurationRetrievalResult,
                mConfiguration.getConnectionBuilder());
    }

    @MainThread
    private void handleConfigurationRetrievalResult(
            AuthorizationServiceConfiguration config,
            AuthorizationException ex) {
        if (config == null) {
            Log.i(TAG, "Failed to retrieve discovery document", ex);
            displayError("Failed to retrieve discovery document: " + ex.getMessage(), true);
            return;
        }

        Log.i(TAG, "Discovery document retrieved");
        mAuthStateManager.replace(new AuthState(config));
        mExecutor.submit(this::initializeClient);
    }

    /**
     * Initiates a dynamic registration request if a client ID is not provided by the static
     * configuration.
     */
    @WorkerThread
    private void initializeClient() {
        if (mConfiguration.getClientId() != null) {
            Log.i(TAG, "Using static client ID: " + mConfiguration.getClientId());
            // use a statically configured client ID
            mClientId.set(mConfiguration.getClientId());
            runOnUiThread(this::initializeAuthRequest);
            return;
        }

        RegistrationResponse lastResponse =
                mAuthStateManager.getCurrent().getLastRegistrationResponse();
        if (lastResponse != null) {
            Log.i(TAG, "Using dynamic client ID: " + lastResponse.clientId);
            // already dynamically registered a client ID
            mClientId.set(lastResponse.clientId);
            runOnUiThread(this::initializeAuthRequest);
            return;
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        runOnUiThread(() -> displayLoading("Dynamically registering client"));
        Log.i(TAG, "Dynamically registering client");

        RegistrationRequest registrationRequest = new RegistrationRequest.Builder(
                mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
                Collections.singletonList(mConfiguration.getRedirectUri()))
                .setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
                .build();

        mAuthService.performRegistrationRequest(
                registrationRequest,
                this::handleRegistrationResponse);
    }

    @MainThread
    private void handleRegistrationResponse(
            RegistrationResponse response,
            AuthorizationException ex) {
        mAuthStateManager.updateAfterRegistration(response, ex);
        if (response == null) {
            Log.i(TAG, "Failed to dynamically register client", ex);
            displayErrorLater("Failed to register client: " + ex.getMessage(), true);
            return;
        }

        Log.i(TAG, "Dynamically registered client: " + response.clientId);
        mClientId.set(response.clientId);
        initializeAuthRequest();
    }

    /**
     * Enumerates the browsers installed on the device and populates a spinner, allowing the
     * demo user to easily test the authorization flow against different browser and custom
     * tab configurations.
     */
//    @MainThread
//    private void configureBrowserSelector() {
//        Spinner spinner = (Spinner) findViewById(R.id.browser_selector);
//        final BrowserSelectionAdapter adapter = new BrowserSelectionAdapter(this);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                BrowserInfo info = adapter.getItem(position);
//                if (info == null) {
//                    mBrowserMatcher = AnyBrowserMatcher.INSTANCE;
//                    return;
//                } else {
//                    mBrowserMatcher = new ExactBrowserMatcher(info.mDescriptor);
//                }
//
//                recreateAuthorizationService();
//                createAuthRequest(getLoginHint());
//                warmUpBrowser();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                mBrowserMatcher = AnyBrowserMatcher.INSTANCE;
//            }
//        });
//    }

    /**
     * Performs the authorization request, using the browser selected in the spinner,
     * and a user-provided `login_hint` if available.
     */
    @WorkerThread
    private void doAuth() {
        try {
            mAuthIntentLatch.await();
        } catch (InterruptedException ex) {
            Log.w(TAG, "Interrupted while waiting for auth intent");
        }

//        if (mUsePendingIntents) {
//            final Intent completionIntent = new Intent(this, TokenActivity.class);
//            final Intent cancelIntent = new Intent(this, LoginActivity.class);
//            cancelIntent.putExtra(EXTRA_FAILED, true);
//            cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            int flags = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                flags |= PendingIntent.FLAG_MUTABLE;
//            }
//
//            mAuthService.performAuthorizationRequest(
//                    mAuthRequest.get(),
//                    PendingIntent.getActivity(this, 0, completionIntent, flags),
//                    PendingIntent.getActivity(this, 0, cancelIntent, flags),
//                    mAuthIntent.get());
//        } else {
            Intent intent = mAuthService.getAuthorizationRequestIntent(
                    mAuthRequest.get(),
                    mAuthIntent.get());
            startActivityForResult(intent, RC_AUTH);
//        }
    }

    private void recreateAuthorizationService() {
        if (mAuthService != null) {
            Log.i(TAG, "Discarding existing AuthService instance");
            mAuthService.dispose();
        }
        mAuthService = createAuthorizationService();
        mAuthRequest.set(null);
        mAuthIntent.set(null);
    }

    private AuthorizationService createAuthorizationService() {
        Log.i(TAG, "Creating authorization service");
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setBrowserMatcher(mBrowserMatcher);
        builder.setConnectionBuilder(mConfiguration.getConnectionBuilder());

        return new AuthorizationService(this, builder.build());
    }

    @MainThread
    private void displayLoading(String loadingMessage) {
//        findViewById(R.id.loading_container).setVisibility(View.VISIBLE);
//        findViewById(R.id.auth_container).setVisibility(View.GONE);
//        findViewById(R.id.error_container).setVisibility(View.GONE);
//
//        ((TextView)findViewById(R.id.loading_description)).setText(loadingMessage);
    }

    @MainThread
    private void displayError(String error, boolean recoverable) {
//        findViewById(R.id.error_container).setVisibility(View.VISIBLE);
//        findViewById(R.id.loading_container).setVisibility(View.GONE);
//        findViewById(R.id.auth_container).setVisibility(View.GONE);
//
//        ((TextView)findViewById(R.id.error_description)).setText(error);
//        findViewById(R.id.retry).setVisibility(recoverable ? View.VISIBLE : View.GONE);
    }

    // WrongThread inference is incorrect in this case
    @SuppressWarnings("WrongThread")
    @AnyThread
    private void displayErrorLater(final String error, final boolean recoverable) {
        runOnUiThread(() -> displayError(error, recoverable));
    }

    @MainThread
    private void initializeAuthRequest() {
        createAuthRequest(null);
        warmUpBrowser();
        displayAuthOptions();
    }

    @MainThread
    private void displayAuthOptions() {
//        findViewById(R.id.auth_container).setVisibility(View.VISIBLE);
//        findViewById(R.id.loading_container).setVisibility(View.GONE);
//        findViewById(R.id.error_container).setVisibility(View.GONE);

        AuthState state = mAuthStateManager.getCurrent();
        AuthorizationServiceConfiguration config = state.getAuthorizationServiceConfiguration();

        String authEndpointStr;
        if (config.discoveryDoc != null) {
            authEndpointStr = "Discovered auth endpoint: \n";
        } else {
            authEndpointStr = "Static auth endpoint: \n";
        }
        authEndpointStr += config.authorizationEndpoint;
//        ((TextView)findViewById(R.id.auth_endpoint)).setText(authEndpointStr);

        String clientIdStr;
        if (state.getLastRegistrationResponse() != null) {
            clientIdStr = "Dynamic client ID: \n";
        } else {
            clientIdStr = "Static client ID: \n";
        }
        clientIdStr += mClientId;
//        ((TextView)findViewById(R.id.client_id)).setText(clientIdStr);
    }

    private void displayAuthCancelled() {
//        Snackbar.make(findViewById(R.id.coordinator),
//                        "Authorization canceled",
//                        Snackbar.LENGTH_SHORT)
//                .show();
    }

    private void warmUpBrowser() {
        mAuthIntentLatch = new CountDownLatch(1);
        mExecutor.execute(() -> {
            Log.i(TAG, "Warming up browser instance for auth request");
            CustomTabsIntent.Builder intentBuilder =
                    mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri());
//            intentBuilder.setToolbarColor(getColorCompat(R.color.colorPrimary));
            mAuthIntent.set(intentBuilder.build());
            mAuthIntentLatch.countDown();
        });
    }

    private void createAuthRequest(@Nullable String loginHint) {
        Log.i(TAG, "Creating auth request for login hint: " + loginHint);
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
                mClientId.get(),
                ResponseTypeValues.CODE,
                mConfiguration.getRedirectUri())
                .setScope(mConfiguration.getScope());

        if (!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint);
        }

        mAuthRequest.set(authRequestBuilder.build());
    }

//    private String getLoginHint() {
//        return ((EditText)findViewById(R.id.login_hint_value))
//                .getText()
//                .toString()
//                .trim();
//    }

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("deprecation")
    private int getColorCompat(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        } else {
            return getResources().getColor(color);
        }
    }

    /**
     * Responds to changes in the login hint. After a "debounce" delay, warms up the browser
     * for a request with the new login hint; this avoids constantly re-initializing the
     * browser while the user is typing.
     */
//    private final class LoginHintChangeHandler implements TextWatcher {
//
//        private static final int DEBOUNCE_DELAY_MS = 500;
//
//        private Handler mHandler;
//        private LoginActivity.RecreateAuthRequestTask mTask;
//
//        LoginHintChangeHandler() {
//            mHandler = new Handler(Looper.getMainLooper());
//            mTask = new LoginActivity.RecreateAuthRequestTask();
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence cs, int start, int count, int after) {}
//
//        @Override
//        public void onTextChanged(CharSequence cs, int start, int before, int count) {
//            mTask.cancel();
//            mTask = new LoginActivity.RecreateAuthRequestTask();
//            mHandler.postDelayed(mTask, DEBOUNCE_DELAY_MS);
//        }
//
//        @Override
//        public void afterTextChanged(Editable ed) {}
//    }

    private final class RecreateAuthRequestTask implements Runnable {

        private final AtomicBoolean mCanceled = new AtomicBoolean();

        @Override
        public void run() {
            if (mCanceled.get()) {
                return;
            }

            createAuthRequest(null);
            warmUpBrowser();
        }

        public void cancel() {
            mCanceled.set(true);
        }
    }
}

