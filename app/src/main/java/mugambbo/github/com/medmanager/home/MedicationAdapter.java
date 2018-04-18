package mugambbo.github.com.medmanager.home;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.constant.Constants;
import mugambbo.github.com.medmanager.interfaces.RecyclerViewListener;
import mugambbo.github.com.medmanager.model.DateItem;
import mugambbo.github.com.medmanager.model.ListItem;
import mugambbo.github.com.medmanager.model.MedicationItem;
import mugambbo.github.com.medmanager.util.DateTimeUtil;

/**
 * Created by Abdulmajid on 4/8/18.
 */

public class MedicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final RecyclerViewListener mListener;
    private List<ListItem> myMedications = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIMESTAMP_PATTERN, Locale.getDefault());
    SimpleDateFormat sdf2 = new SimpleDateFormat(Constants.HUMAN_READABLE_DATETIME_PATTERN, Locale.getDefault());
    SimpleDateFormat sdf3 = new SimpleDateFormat(Constants.MONTH_PATTERN, Locale.getDefault());


    // ViewHolder for date row item
    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView mMedMonth;
        DateViewHolder(View v) {
            super(v);
            mMedMonth = v.findViewById(R.id.tv_med_month);
        }
    }

    public MedicationAdapter(RecyclerViewListener listener) {
        this.mListener = listener;
    }

    // View holder for medication row item
    class MedicationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mMedName;
        TextView mMedDescription;
        TextView mMedDueDate;
        TextView mMedNoLeft;
        CircularProgressBar mMedNoLeftProgress;
        ProgressBar mMedNextDueDateProgress;

        MedicationViewHolder(View v) {
            super(v);
            mMedName = (TextView) v.findViewById(R.id.tv_med_name);
            mMedDescription = (TextView) v.findViewById(R.id.tv_med_description);
            mMedDueDate = (TextView) v.findViewById(R.id.tv_med_due_date);
            mMedNoLeft = (TextView) v.findViewById(R.id.tv_med_no_left_val);
            mMedNoLeftProgress = (CircularProgressBar) v.findViewById(R.id.tv_med_no_left_progress);
            mMedNextDueDateProgress = (ProgressBar) v.findViewById(R.id.pb_med_next_due_date);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onMedicationItemClick(v, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ListItem.TYPE_MEDICATION:
                View medView = inflater.inflate(R.layout.list_item_medication_layout, parent, false);
                viewHolder = new MedicationViewHolder(medView);
                break;

            case ListItem.TYPE_DATE:
                View dateView = inflater.inflate(R.layout.list_item_date_layout, parent, false);
                viewHolder = new DateViewHolder(dateView);
                break;

            default:
                View medViewX = inflater.inflate(R.layout.list_item_medication_layout, parent, false);
                viewHolder = new MedicationViewHolder(medViewX);
                break;
        }

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {

            case ListItem.TYPE_MEDICATION:
                MedicationItem medicationItem = (MedicationItem) myMedications.get(position);
                MedicationViewHolder medicationItemViewHolder = (MedicationViewHolder) viewHolder;
                medicationItemViewHolder.mMedName.setText(medicationItem.getMedName());
                medicationItemViewHolder.mMedDescription.setText(medicationItem.getMedDescription());
//                Log.e("MedMan", "Total dosage: Remaining dosage: Percentage: ");
                try {
                    long startDate = TimeUnit.MILLISECONDS.toHours(DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN));
                    long endDate = TimeUnit.MILLISECONDS.toHours(DateTimeUtil.convertDateToMillis(medicationItem.getMedEndDate(), Constants.TIMESTAMP_PATTERN));
                    long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

//                    long nextDueDate = (endDate - startDate)

//                    medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Next Due: "+sdf2.format(sdf.parse(medicationItem.getMedStartDate()))));

//                    medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Next Due: "+sdf2.format(sdf.parse(medicationItem.getMedStartDate()))));

                    int totalDosage = (int)(endDate - startDate)/medicationItem.getMedInterval() + 1;
                    int remainingDosage;
                    long dueDate;
                    double nextDueDatePercent;
                    double dosageLeftPercent  ;
                    if (startDate <= now && endDate >= now) {
                        remainingDosage = (int)(endDate - now)/medicationItem.getMedInterval() + 1;
                        medicationItemViewHolder.mMedNoLeft.setText(String.valueOf(remainingDosage + " more"+"\n"+"doses left"));
                        dueDate = DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN) + TimeUnit.HOURS.toMillis((totalDosage - remainingDosage) * medicationItem.getMedInterval());
                        nextDueDatePercent = 100.0 - (double) (dueDate - System.currentTimeMillis())/TimeUnit.HOURS.toMillis(medicationItem.getMedInterval()) * 100.0;
                        medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Next Due: "+DateTimeUtil.getRelativeTime(dueDate)));
                        dosageLeftPercent = (double) (totalDosage - remainingDosage)/totalDosage * 100.0;
                    } else if (now > endDate) {
                        medicationItemViewHolder.mMedNoLeft.setText(R.string.all_done);
                        nextDueDatePercent = 0;
                        medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Finished"));
                        dosageLeftPercent = 0.0;
                    } else {
                        remainingDosage = totalDosage;
                        dueDate = DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN) + TimeUnit.HOURS.toMillis((totalDosage - remainingDosage) * medicationItem.getMedInterval());
                        nextDueDatePercent = 0;
                        medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Next Due: "+DateTimeUtil.getRelativeTime(dueDate)));
                        dosageLeftPercent = 0;
                    }

//                    long dueDate = DateTimeUtil.convertDateToMillis(medicationItem.getMedStartDate(), Constants.TIMESTAMP_PATTERN) + TimeUnit.HOURS.toMillis((totalDosage - remainingDosage) * medicationItem.getMedInterval());
//                    medicationItemViewHolder.mMedDueDate.setText(String.valueOf("Next Due: "+DateTimeUtil.convertDateFromMillis(dueDate, Constants.HUMAN_READABLE_DATETIME_PATTERN)));

//                    Log.d("", "Total dosage: "+totalDosage+" Remaining dosage: "+remainingDosage+" Percentage: "+dosageLeftPercent);
//                    Log.d("", "Due date: "+dueDate+"Current Time: "+System.currentTimeMillis()+" Next Due Percentage: "+nextDueDatePercent);
                    medicationItemViewHolder.mMedNoLeftProgress.setProgress((int)dosageLeftPercent);
                    medicationItemViewHolder.mMedNextDueDateProgress.setProgress((int)nextDueDatePercent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case ListItem.TYPE_DATE:
                DateItem dateItem = (DateItem) myMedications.get(position);
                DateViewHolder dateViewHolder = (DateViewHolder) viewHolder;
                dateViewHolder.mMedMonth.setText(dateItem.getMedStartDate().toUpperCase());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return myMedications.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return myMedications != null ? myMedications.size() : 0;
    }

    void updateMedicationList(List<ListItem> listItems){
        this.myMedications.clear();
        this.myMedications.addAll(listItems);
        notifyDataSetChanged();
    }
}
