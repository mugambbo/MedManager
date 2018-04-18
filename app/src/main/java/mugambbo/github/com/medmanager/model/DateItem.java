package mugambbo.github.com.medmanager.model;

/**
 * Created by abdulmajid on 4/8/18.
 */

public class DateItem extends ListItem {
    String medStartDate;
    String medEndDate;

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
        return TYPE_DATE;
    }
}
