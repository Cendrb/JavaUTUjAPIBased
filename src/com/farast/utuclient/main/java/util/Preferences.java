package com.farast.utuclient.main.java.util;

/**
 * Created by cendr_000 on 28.07.2016.
 */
public final class Preferences {

    private static java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userRoot();
    private static int sclassId;

    static {
        sclassId = preferences.getInt("sclassId", -1);
    }

    private Preferences() {

    }

    public static int getSclassId() {
        return sclassId;
    }

    public static void setSclassId(int sclassId) {
        preferences.putInt("sclassId", sclassId);
        Preferences.sclassId = sclassId;
    }
}
