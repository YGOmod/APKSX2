package kr.co.iefriends.pcsx2;

import android.content.SharedPreferences;
import android.util.ArraySet;
import java.util.Set;

public class PrefHelper {
    public static boolean addToStringList(SharedPreferences sharedPreferences, String key, String value) {
        ArraySet arraySet;
        Set<String> stringSet = getStringSet(sharedPreferences, key);
        if (stringSet == null) {
            arraySet = new ArraySet();
        } else {
            ArraySet arraySet2 = new ArraySet();
            arraySet2.addAll(stringSet);
            arraySet = arraySet2;
        }
        boolean add = arraySet.add(value);
        sharedPreferences.edit().putStringSet(key, arraySet).commit();
        return add;
    }
}