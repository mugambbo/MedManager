package mugambbo.github.com.medmanager.home;

import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public interface DateChangeListener{
    void onDateSelected(DatePicker view, Calendar calendar, String tag);
    void onTimeSelected(TimePicker view, int hourOfDay, int minute, String tag);
}
