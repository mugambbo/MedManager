package mugambbo.github.com.medmanager.model;

/**
 * Created by abdulmajid on 4/8/18.
 */

public abstract class ListItem {
    public static final int TYPE_DATE = 0;
    public static final int TYPE_MEDICATION = 1;
    public abstract int getType();
}
