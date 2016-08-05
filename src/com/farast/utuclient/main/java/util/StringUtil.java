package com.farast.utuclient.main.java.util;

/**
 * Created by cendr_000 on 28.07.2016.
 */
public final class StringUtil {
    private StringUtil()
    {

    }

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
