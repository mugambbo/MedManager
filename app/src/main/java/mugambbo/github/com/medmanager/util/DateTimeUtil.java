package mugambbo.github.com.medmanager.util;

import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Abdulmajid on 4/10/18.
 */

public class DateTimeUtil {
    public static String changeDateFormat (String currFormat, String reqFormat, String dateString) throws ParseException {
        String result = "";
        if (TextUtils.isEmpty(dateString)){
            return result;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(currFormat, Locale.getDefault());
        SimpleDateFormat sdf2 = new SimpleDateFormat(reqFormat, Locale.getDefault());
        Date date;
        date = sdf.parse(dateString);
        if (date != null){
            result = sdf2.format(date);
        }

        return result;
    }

    public static long convertDateToMillis(String date, String format) throws ParseException {
        return new SimpleDateFormat(format, Locale.getDefault()).parse(date).getTime();
    }

    public static String convertDateFromMillis(long millis, String datePattern){
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }

    public static String getRelativeTime(long timeMillis){
        return String.valueOf(DateUtils.getRelativeTimeSpanString(timeMillis, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));
    }
}
