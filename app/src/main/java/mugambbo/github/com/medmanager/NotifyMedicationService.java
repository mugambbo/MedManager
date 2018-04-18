package mugambbo.github.com.medmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.text.DateFormat;
import java.util.Date;

import mugambbo.github.com.medmanager.home.HomeActivity;
import mugambbo.github.com.medmanager.model.MedicationItem;
import mugambbo.github.com.medmanager.util.NotificationHelper;

public class NotifyMedicationService extends JobService {
    private static final String TAG = "MyJobService";

    public NotifyMedicationService() {

    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Bundle bundle = jobParameters.getExtras();
        if (bundle != null){
            MedicationItem medicationItem = (MedicationItem) bundle.getSerializable("medication");

//            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
//            Toast.makeText(this, "Start Job", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, HomeActivity.class);
            NotificationHelper notificationHelper = new NotificationHelper(this, getString(R.string.time_to_take_your)+" "+medicationItem.getMedName());
            NotificationCompat.Builder builder = notificationHelper.getNotificationBuilder(medicationItem.getMedName(), medicationItem.getMedDescription(), intent);
            notificationHelper.getNotificationManager().notify(101, builder.build());
            jobFinished(jobParameters, false);
            HomeActivity homeActivity = new HomeActivity();
            homeActivity.cancelJob(HomeActivity.JOB_TAG);
            homeActivity.scheduleNextDueMedication();
        }
//        HomeActivity.scheduleJob();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled!");
        Toast.makeText(this, "Stop Job", Toast.LENGTH_LONG).show();
        return false;
    }
}
