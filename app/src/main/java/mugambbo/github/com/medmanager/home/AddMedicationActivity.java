package mugambbo.github.com.medmanager.home;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.constant.Constants;
import mugambbo.github.com.medmanager.database.AppDatabase;
import mugambbo.github.com.medmanager.model.MedicationItem;
import mugambbo.github.com.medmanager.util.DateTimeUtil;

public class AddMedicationActivity extends AppCompatActivity implements DateChangeListener{

    private TextInputEditText mMedName, mMedDescription, mMedInterval;
    private EditText mMedStartDate;
    private EditText mMedEndDate;
    private final int START_DATE_CODE = 0;
    private final int END_DATE_CODE = 1;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.HUMAN_READABLE_DATETIME_PATTERN, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMedName = (TextInputEditText) findViewById(R.id.et_med_name);
        mMedDescription = (TextInputEditText) findViewById(R.id.et_med_description);
        mMedInterval = (TextInputEditText) findViewById(R.id.et_med_interval);
        mMedStartDate = (EditText) findViewById(R.id.et_start_date);
        mMedEndDate = (EditText) findViewById(R.id.et_end_date);
        final Button mAddBtn = (Button) findViewById(R.id.btn_add_medication);

        //initialize start and end dates
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        mMedStartDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, 5);
        mMedEndDate.setText(sdf.format(cal.getTime()));

        mMedStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(AddMedicationActivity.this, START_DATE_CODE);
            }
        });

        mMedEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(AddMedicationActivity.this, END_DATE_CODE);
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAndSaveMedication();
            }
        });

        final int medID = getIntent().getIntExtra(MedicationListFragment.SELECTED_MEDICATION, -1);
        if (medID != -1){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //Fetch medication at this position
                    final MedicationItem medicationItem = AppDatabase.getInstance(AddMedicationActivity.this).medicationItemDao().getOneMedication(medID);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMedName.setText(medicationItem.getMedName());
                            mMedDescription.setText(medicationItem.getMedDescription());
                            try {
                                mMedInterval.setText(String.valueOf(medicationItem.getMedInterval().intValue()));
                                mMedStartDate.setText(DateTimeUtil.changeDateFormat(Constants.TIMESTAMP_PATTERN, Constants.HUMAN_READABLE_DATETIME_PATTERN, medicationItem.getMedStartDate()));
                                mMedEndDate.setText(DateTimeUtil.changeDateFormat(Constants.TIMESTAMP_PATTERN, Constants.HUMAN_READABLE_DATETIME_PATTERN, medicationItem.getMedEndDate()));
                            } catch (Exception err){
                                err.printStackTrace();
                                mMedStartDate.setText(medicationItem.getMedStartDate());
                                mMedEndDate.setText(medicationItem.getMedEndDate());
                            }

                            mMedName.setEnabled(false);
                            mMedDescription.setEnabled(false);
                            mMedInterval.setEnabled(false);
                            mMedStartDate.setEnabled(false);
                            mMedEndDate.setEnabled(false);

                            setTitle("My Medication");
                            mAddBtn.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    private void verifyAndSaveMedication(){
        try {
            final String medName = mMedName.getText().toString();
            final String medDescr = mMedDescription.getText().toString();
            final String medInterval = mMedInterval.getText().toString();
            final String medStartDate = DateTimeUtil.changeDateFormat(Constants.HUMAN_READABLE_DATETIME_PATTERN, Constants.TIMESTAMP_PATTERN, mMedStartDate.getText().toString());
            final String medEndDate = DateTimeUtil.changeDateFormat(Constants.HUMAN_READABLE_DATETIME_PATTERN, Constants.TIMESTAMP_PATTERN, mMedEndDate.getText().toString());

            if (TextUtils.isEmpty(medName.trim()) || TextUtils.isEmpty(medDescr.trim()) || TextUtils.isEmpty(medInterval.trim())) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_LONG).show();
            } else {
                //save medication
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        MedicationItem medicationItem = new MedicationItem((int) System.currentTimeMillis() / 1000, medName, medDescr, Integer.parseInt(medInterval.trim()), medStartDate, medEndDate);
                        AppDatabase db = AppDatabase.getInstance(AddMedicationActivity.this);
                        db.medicationItemDao().insertMedication(medicationItem);

                        AddMedicationActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddMedicationActivity.this, "Saved", Toast.LENGTH_LONG).show();
                                NavUtils.navigateUpFromSameTask(AddMedicationActivity.this);

                            }
                        });
                    }
                });
            }
        } catch (Exception err){
            err.printStackTrace();
            Toast.makeText(this, "Could not save medication", Toast.LENGTH_LONG).show();
        }
    }

    public void showTimePickerDialog(DateChangeListener onDateChangeListener, int startOrEnd) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setDateChange(onDateChangeListener);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker"+startOrEnd);
    }

    public void showDatePickerDialog(DateChangeListener onDateChangeListener, int startOrEnd) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setDateChange(onDateChangeListener);
        datePickerFragment.show(getSupportFragmentManager(), "datePicker"+startOrEnd);
    }

    @Override
    public void onDateSelected(DatePicker view, Calendar calendar, String tag) {
        this.calendar = calendar;
        if (tag.equalsIgnoreCase("datePicker"+START_DATE_CODE)) {
            mMedStartDate.setText(sdf.format(calendar.getTime()));
            showTimePickerDialog(this, START_DATE_CODE);
        } else {
            mMedEndDate.setText(sdf.format(calendar.getTime()));
            showTimePickerDialog(this, END_DATE_CODE);
        }
    }

    @Override
    public void onTimeSelected(TimePicker view, int hourOfDay, int minute, String tag) {
        this.calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        this.calendar.set(Calendar.MINUTE, minute);
        if (tag.equalsIgnoreCase("timePicker"+START_DATE_CODE)){
            mMedStartDate.setText(sdf.format(calendar.getTime()));
        } else {
            mMedEndDate.setText(sdf.format(calendar.getTime()));
        }
    }


    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        DateChangeListener dateChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user

            dateChangeListener.onTimeSelected(view, hourOfDay, minute, getTag());
        }

        public void setDateChange(DateChangeListener dateChangeListener){
            this.dateChangeListener = dateChangeListener;
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        DateChangeListener onDateChangeListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);

            onDateChangeListener.onDateSelected(view, cal, getTag());

            // Do something with the date chosen by the user
        }

        public void setDateChange(DateChangeListener dateChangeListener){
            this.onDateChangeListener = dateChangeListener;
        }
    }


}


