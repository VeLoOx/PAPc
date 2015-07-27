package pl.pap.utils;



public interface Consts {
	// /@class path/@method path
	public String domainAdress="http://192.168.88.252:10080/PAPserver";
	public String LOGIN="/login/dologin";
	public String REGISTER="/register/doregister";
	public String GET_USER_INFO="/userinfo/getinfo";
	public String PERSIST_ROUTE="/persistRoute/persistroute";
	public String REQUEST_ROUTE="/requestRoute/requestroute";
	
	//Sharedpreferences
	public String PREFS="clientPrefs";
	public String USER_LOGIN = "loginKey";
	public String USER_PASS = "passwordKey";
	public String USER_SESSINID = "sessionIDKey";
	
}
