package mugambbo.github.com.medmanager.database;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import mugambbo.github.com.medmanager.model.MedicationItem;

/**
 * Created by abdulmajid on 4/8/18.
 */

@Database(entities = {MedicationItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    @VisibleForTesting
    private static final String DATABASE_NAME = "medication";
    private static AppDatabase sInstance;

    public abstract MedicationItemDao medicationItemDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

}

