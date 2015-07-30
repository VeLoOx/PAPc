package pl.pap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefsUtils implements Consts {
	SharedPreferences prefs;
	Context context;
	Editor editor; 
	
	public SharedPrefsUtils(Context context){
		this.context=context;
		prefs = this.context.getSharedPreferences(
			     PREFS, Context.MODE_PRIVATE);
	}
	
	public String getLogin(){
		return prefs.getString(USER_LOGIN, "");
	}
	
	public boolean checkLogin(){
		return prefs.contains(USER_LOGIN);
	}
	
	public void setLogin(String login){
		editor= prefs.edit();
		editor.putString(USER_LOGIN, login);
		editor.commit();
	}
	
	public String getPassword(){
		return prefs.getString(USER_PASS, "");
	}
	public boolean checkPassword(){
		return prefs.contains(USER_PASS);
	}
	public void setPassword(String password){
		editor=prefs.edit();
		editor.putString(USER_PASS, password);
		editor.commit();
	}
	
	public String getSessionID(){
		return prefs.getString(USER_SESSINID, "");
	}
	
	public boolean checkSessionID(){
		return prefs.contains(USER_SESSINID);
	}
	
	public void setSessionID(String sessionID){
		editor = prefs.edit();
		editor.putString(USER_SESSINID, sessionID);
		editor.commit();
	}
	
}
