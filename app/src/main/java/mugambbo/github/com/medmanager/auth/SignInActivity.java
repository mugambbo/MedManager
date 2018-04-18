package mugambbo.github.com.medmanager.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.util.SharedPreferencesUtil;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "GoogleSignIn";
    private ProgressBar mSignInProgress;
    private TextInputEditText mEmail, mPassword;
    private Button mLoginBtn;
    private LinearLayout mGoogleLogin;
    public final String USER_EMAIL_KEY = "UserEmail";
    public static final String USER_PASSWORD_KEY = "UserPassword";
    public final String USER_FIRST_NAME = "UserFirstName";
    public final String USER_LAST_NAME = "UserLastName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmail = (TextInputEditText) findViewById(R.id.et_email);
        mPassword = (TextInputEditText) findViewById(R.id.et_password);
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mGoogleLogin = (LinearLayout) findViewById(R.id.linear_layout_google_login);
        mSignInProgress = (ProgressBar) findViewById(R.id.pb_login);
        RelativeLayout mRelLayoutSignUp = (RelativeLayout) findViewById(R.id.rel_layout_sign_up);
        configureGoogleSign();
        mLoginBtn.setOnClickListener(this);
        mGoogleLogin.setOnClickListener(this);
        mRelLayoutSignUp.setOnClickListener(this);
    }

    private void configureGoogleSign (){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("976783651910-7fjp2fq13gjm2n8900jd81e5id59i00e.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }


    private void signUserIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        LauncherHelper.launchHomeActivity(this, currentUser);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_login:
                signNewUserIn();
                break;
            case R.id.linear_layout_google_login:
                signUserIn();
                break;
            case R.id.rel_layout_sign_up:
                LauncherHelper.launchSignUpActivity(this);
        }
    }

    private void signNewUserIn() {
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        if (TextUtils.isEmpty(email.trim()) || TextUtils.isEmpty(password.trim())){
            Snackbar.make(mEmail, "All fields are required", Snackbar.LENGTH_LONG).show();
        } else {
            showProgressBar();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                //Save user details in shared preferences
                                SharedPreferencesUtil.getInstance(getApplicationContext()).putString(USER_PASSWORD_KEY, password);
                                LauncherHelper.launchHomeActivity(SignInActivity.this, user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                LauncherHelper.launchHomeActivity(SignInActivity.this, null);
                            }

                            hideProgressBar();
                            // ...
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
//                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                LauncherHelper.launchHomeActivity(this, null);
                // [END_EXCLUDE]
            }
        }
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressBar();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, Log user in with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            LauncherHelper.launchHomeActivity(SignInActivity.this, user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.btn_login), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            LauncherHelper.launchHomeActivity(SignInActivity.this, null);
                        }

                        // [START_EXCLUDE]
                        hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void showProgressBar() {
        if (mSignInProgress != null){
            mSignInProgress.setVisibility(View.VISIBLE);
            mEmail.setEnabled(false);
            mPassword.setEnabled(false);
            mLoginBtn.setEnabled(false);
            mGoogleLogin.setEnabled(false);

        }
    }

    private void hideProgressBar() {
        if (mSignInProgress != null){
            mSignInProgress.setVisibility(View.GONE);
            mEmail.setEnabled(true);
            mPassword.setEnabled(true);
            mLoginBtn.setEnabled(true);
            mGoogleLogin.setEnabled(true);
        }
    }
// [END auth_with_google]
}
