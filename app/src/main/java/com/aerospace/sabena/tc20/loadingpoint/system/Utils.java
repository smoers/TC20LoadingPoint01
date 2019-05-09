package com.aerospace.sabena.tc20.loadingpoint.system;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean findString(String pattern, String str){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String groupOneString(String pattern, String str){
        String result = null;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        if (m.find())
            result = m.group(1);
        return result;
    }

}
