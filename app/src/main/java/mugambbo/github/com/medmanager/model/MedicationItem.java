package mugambbo.github.com.medmanager.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by abdulmajid on 4/8/18.
 */

@Entity
public class MedicationItem extends ListItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "med_id")
    private int medId;
    @ColumnInfo(name = "med_name")
    String medName;
    @ColumnInfo(name = "med_description")
    String medDescription;
    @ColumnInfo(name = "med_interval")
    Integer medInterval;
    @ColumnInfo(name = "med_start_date")
    String medStartDate;
    @ColumnInfo(name = "med_end_date")
    String medEndDate;

    public MedicationItem() {
    }

    @Ignore
    public MedicationItem(int medId, String medName, String medDescription, Integer medInterval, String medStartDate, String medEndDate) {
        this.medId = medId;
        this.medName = medName;
        this.medDescription = medDescription;
        this.medInterval = medInterval;
        this.medStartDate = medStartDate;
        this.medEndDate = medEndDate;
    }

    public String getMedName() {
        return medName;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public String getMedDescription() {
        return medDescription;
    }

    public void setMedDescription(String medDescription) {
        this.medDescription = medDescription;
    }

    public Integer getMedInterval() {
        return medInterval;
    }

    public void setMedInterval(Integer medInterval) {
        this.medInterval = medInterval;
    }

    public String getMedStartDate() {
        return medStartDate;
    }

    public void setMedStartDate(String medStartDate) {
        this.medStartDate = medStartDate;
    }

    public String getMedEndDate() {
        return medEndDate;
    }

    public void setMedEndDate(String medEndDate) {
        this.medEndDate = medEndDate;
    }

    @Override
    public int getType(){
        return TYPE_MEDICATION;
    }

    public int getMedId() {
        return medId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
