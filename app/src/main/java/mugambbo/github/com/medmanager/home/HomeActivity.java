package mugambbo.github.com.medmanager.home;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

import mugambbo.github.com.medmanager.NotifyMedicationService;
import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.auth.LauncherHelper;
import mugambbo.github.com.medmanager.constant.Constants;
import mugambbo.github.com.medmanager.database.AppDatabase;
import mugambbo.github.com.medmanager.model.MedicationItem;
import mugambbo.github.com.medmanager.util.DateTimeUtil;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, MedicationListFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SearchView.OnQueryTextListener {

    private NavigationView navigationView;
    private Fragment fragment;
    private ImageView mNavProfileImage;
    private TextView mNavDisplayName;
    private TextView mNavEmail;
    private static FirebaseJobDispatcher mDispatcher;
    public static String JOB_TAG = "NotifyMedicationJobTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        checkPermission(this);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNavProfileImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.iv_nav_profile_image);
        mNavDisplayName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_nav_user_display_name);
        mNavEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_nav_user_email);

        initViews();
        displaySelectedScreen(navigationView.getMenu().getItem(0), R.id.nav_meds);
    }

    private void initViews (){

    }

    private void updateProfileInfo(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).apply(RequestOptions.circleCropTransform().error(R.drawable.ic_profile_placeholder)).into(mNavProfileImage);
        }
        mNavDisplayName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        mNavEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileInfo();
        cancelJob(null);
        scheduleNextDueMedication();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.search);
        if (fragment instanceof MedicationListFragment){
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(item, id);
        return true;
    }

    private void displaySelectedScreen(final MenuItem item, final int id){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (id == R.id.nav_meds) {
                    fragment = new MedicationListFragment();
                    setTitle(getString(R.string.my_medications));
                } else if (id == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                    setTitle(getString(R.string.my_profile));
                } else if (id == R.id.nav_share) {
                    Intent shareIntent = ShareCompat.IntentBuilder.from(HomeActivity.this)
                            .setType("text/plain")
                            .setText(getString(R.string.share_med_manager_text))
                            .getIntent();
                    if (shareIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(shareIntent);
                    }
                } else if (id == R.id.nav_logout) {
                    signOut();
                }

                if (fragment != null){
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.constr_content_main, fragment);
                    fragmentTransaction.commit();
                }
            }
        }, 200);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        item.setChecked(true);
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
//        int id = v.getId();
//        switch (id){
//            case R.id.btn_log_out:
//                signOut();
//                break;
//        }
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (fragment instanceof MedicationListFragment){
                ((MedicationListFragment) fragment).queryMedicationDatabase(query);
            }
            //use the query to search your data somehow
        }
    }


    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();

//         [END config_signin]

        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(getApplicationContext()).medicationItemDao().clearAll();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                LauncherHelper.launchSignInActivity(HomeActivity.this);
                            }
                        });
                    }
                });
            }
        });
    }

    public void setDisplayScreen(int screen){
        displaySelectedScreen(navigationView.getMenu().getItem(0), R.id.nav_meds);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        if (ProfileFragment.PROFILE_UPDATED_URI.equals(uri) && FirebaseAuth.getInstance().getCurrentUser() != null){
            updateProfileInfo();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ((MedicationListFragment) fragment).queryMedicationDatabase(newText);
        return true;
    }


    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermission(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.permission_necessary);
        alertBuilder.setMessage(msg + getString(R.string.permission_is_necessary));
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(HomeActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public static void scheduleJob(MedicationItem medicationItem, int timeSeconds) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("medication", medicationItem);
        Job myJob = mDispatcher.newJobBuilder()
                .setService(NotifyMedicationService.class)
                .setTag(JOB_TAG)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(timeSeconds, timeSeconds + 2))
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setExtras(bundle)
                .build();
        mDispatcher.mustSchedule(myJob);
//        Toast.makeText(this, "Job scheduled", Toast.LENGTH_LONG).show();
    }

    public void cancelJob(String jobTag) {
        if ("".equals(jobTag)) {
            mDispatcher.cancelAll();
        } else {
            mDispatcher.cancel(jobTag);
        }
//        Toast.makeText(this, "Job cancelled", Toast.LENGTH_LONG).show();
    }

    public void scheduleNextDueMedication(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final MedicationItem medicationItem = AppDatabase.getInstance(getApplicationContext()).medicationItemDao().getNextDueMedication();
                if (medicationItem != null){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                long startDate = TimeUnit.MILLISECONDS.toHours(DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN));
                                long endDate = TimeUnit.MILLISECONDS.toHours(DateTimeUtil.convertDateToMillis(medicationItem.getMedEndDate(), Constants.TIMESTAMP_PATTERN));
                                long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

                                int totalDosage = (int) (endDate - startDate) / medicationItem.getMedInterval() + 1;
                                int remainingDosage = (int) (endDate - now) / medicationItem.getMedInterval() + 1;
                                long dueDate = DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN) + TimeUnit.HOURS.toMillis((totalDosage - remainingDosage) * medicationItem.getMedInterval());
                                scheduleJob(medicationItem, (int)TimeUnit.MILLISECONDS.toSeconds(dueDate));

                            } catch (Exception err){err.printStackTrace();}
                        }
                    });
                }
            }
        });
    }
}
