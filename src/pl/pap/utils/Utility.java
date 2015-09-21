package pl.pap.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.pap.model.MarkerModel;
import pl.pap.model.Route;

import com.google.gson.Gson;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class Utility {
    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
    
    public static boolean isNotTooShort(String txt){
    	return txt.trim().length()>3;
    }
    
    public static String convertToJson(Object obj){
    	Gson gson = new Gson();
		return gson.toJson(obj);
    }
    
    public static Route convertRouteFromJson(String json){
    	Gson gson = new Gson();
		Route route = gson.fromJson(json, Route.class);
		return route;
    }
}
