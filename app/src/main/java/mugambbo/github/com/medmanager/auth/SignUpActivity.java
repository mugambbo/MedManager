package mugambbo.github.com.medmanager.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.util.SharedPreferencesUtil;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText mName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private TextInputEditText mConfirmPassword;
    private FirebaseAuth mFirebaseAuth;
    private ConstraintLayout mRootLayout;
    private ProgressBar mSignupProgress;
    private RelativeLayout mSignInRel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mRootLayout = (ConstraintLayout) findViewById(R.id.root_layout_sign_up);
        mName = (TextInputEditText) findViewById(R.id.et_name);
        mEmail = (TextInputEditText) findViewById(R.id.et_email);
        mPassword = (TextInputEditText) findViewById(R.id.et_password);
        mConfirmPassword = (TextInputEditText) findViewById(R.id.et_confirm_password);
        mSignInRel = (RelativeLayout) findViewById(R.id.rel_layout_sign_in);
        Button mButton = (Button) findViewById(R.id.btn_sign_up);
        mSignupProgress = (ProgressBar) findViewById(R.id.pb_sign_up);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mButton.setOnClickListener(this);
        mSignInRel.setOnClickListener(this);
    }

    private void verifySignUpDetails(){
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(name.trim()) || TextUtils.isEmpty(email.trim()) || TextUtils.isEmpty(password.trim()) || TextUtils.isEmpty(confirmPassword.trim())){
            Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_LONG).show();
        } else if (!TextUtils.equals(password, confirmPassword)){
            Toast.makeText(SignUpActivity.this, "Password do not match", Toast.LENGTH_LONG).show();
        } else {
            signUpUser(name, email, password);
        }
    }

    private void signUpUser(final String name, String email, final String password){
        showProgress();
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                try {
                    if (task.isSuccessful()){
                        SharedPreferencesUtil.getInstance(getApplicationContext()).putString(SignInActivity.USER_PASSWORD_KEY, password);
                        assert FirebaseAuth.getInstance().getCurrentUser() != null;
                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
                        LauncherHelper.launchHomeActivity(SignUpActivity.this, task.getResult(ApiException.class).getUser());
                    } else {
                        Toast.makeText(SignUpActivity.this, task.getException() != null? task.getException().getMessage():"", Toast.LENGTH_LONG).show();
                    }
                } catch (ApiException err){
                    err.printStackTrace();
                    Toast.makeText(SignUpActivity.this, err.getMessage(), Toast.LENGTH_LONG).show();
                }

                hideProgress();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_sign_up:
                verifySignUpDetails();
                break;
            case R.id.rel_layout_sign_in:
                LauncherHelper.launchSignInActivity(this);
                break;
        }
    }

    private void hideProgress(){
        if (mSignupProgress != null){
            mSignupProgress.setVisibility(View.GONE);
            mRootLayout.setVisibility(View.VISIBLE);
            mSignInRel.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(){
        if (mSignupProgress != null){
            mSignupProgress.setVisibility(View.VISIBLE);
            mRootLayout.setVisibility(View.GONE);
            mSignInRel.setVisibility(View.GONE);
        }
    }


}
