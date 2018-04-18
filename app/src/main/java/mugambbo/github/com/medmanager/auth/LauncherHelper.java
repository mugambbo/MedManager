package mugambbo.github.com.medmanager.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseUser;

import mugambbo.github.com.medmanager.home.HomeActivity;

/**
 * Created by Abdulmajid on 4/7/18.
 */

public class LauncherHelper {
    public static void launchHomeActivity(Activity ctx, FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent homeActivityIntent = new Intent(ctx, HomeActivity.class);
            ctx.startActivity(homeActivityIntent);
            ctx.finish();
        }
    }

    public static void launchSignInActivity(Activity ctx) {
        Intent signInActivityIntent = new Intent(ctx, SignInActivity.class);
        ctx.startActivity(signInActivityIntent);
        ctx.finish();
    }

    public static void launchSignUpActivity(Activity ctx) {
        Intent signUpActivityIntent = new Intent(ctx, SignUpActivity.class);
        ctx.startActivity(signUpActivityIntent);
        ctx.finish();
    }

}
