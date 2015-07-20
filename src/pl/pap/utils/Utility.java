package pl.pap.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class Utility {
    private static Pattern pattern;
    private static Matcher matcher;
    //Email Pattern
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
    /**
     * Validate Email with regular expression
     *
     * @param email
     * @return true for Valid Email and false for Invalid Email
     */
    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
 
    }
    /**
     * Checks for Null String object
     *
     * @param txt
     * @return true for not null and false for null String object
     */
    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
    
    /**
     * Get process name by its PID
     * @param context
     * @param pid
     * @return
     */
    public static String getAppNameByPID(Context context, int pid){
        ActivityManager manager 
                   = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for(RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
            if(processInfo.pid == pid){
                return processInfo.processName;
            }
        }
        return "";
    }
}
