package mugambbo.github.com.medmanager.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mugambbo.github.com.medmanager.model.MedicationItem;

/**
 * Created by abdulmajid on 4/8/18.
 */

@Dao
public interface MedicationItemDao {
    @Query("SELECT * FROM MedicationItem")
    List<MedicationItem> getAll();

    @Query("SELECT * FROM MedicationItem WHERE med_id = :medID LIMIT 1")
    MedicationItem getOneMedication(int medID);

    @Query("SELECT * FROM MedicationItem ORDER BY med_start_date DESC LIMIT 1")
    MedicationItem getNextDueMedication();

    @Query("SELECT * FROM MedicationItem WHERE med_name LIKE :medName ORDER BY :medName")
    List<MedicationItem> getMedicationsByName(String medName);

    @Insert
    void insertMedication(MedicationItem medicationItem);

    @Delete
    void deleteMedication(MedicationItem medicationItem);

    @Insert
    void insertAll(List<MedicationItem> medicationItems);

    @Update
    void updateMedication(MedicationItem medicationItem);

    @Query("DELETE FROM MedicationItem")
    public void clearAll();

}
