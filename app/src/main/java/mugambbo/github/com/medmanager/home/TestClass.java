package mugambbo.github.com.medmanager.home;

import android.text.TextUtils;
import android.util.SparseIntArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by abdulmajid on 4/14/18.
 */

import java.util.Iterator;
import java.io.Reader;

class TestClass implements Iterable<Integer> {
    private List<Integer> integerList = new ArrayList<>();

    public TestClass(Reader inp) {
        BufferedReader br = new BufferedReader(inp);
        try {
            int validInteger = 0;
            assert validInteger < Math.pow(10, 6) && validInteger > -Math.pow(10, 6);
        } catch (Exception e){}

    }

    public Iterator<Integer> iterator() {
        return integerList.iterator();
    }
}
