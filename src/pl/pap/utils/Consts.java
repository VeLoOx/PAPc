package pl.pap.utils;



public interface Consts {
	// /@class path/@method path
	public String domainAdress="http://192.168.88.252:10080/PAPserver";
	public String LOGIN="/login/dologin";
	public String REGISTER="/register/doregister";
	public String GET_USER_INFO="/userinfo/getinfo";
	public String PERSIST_ROUTE="/persistRoute/persistRoute";
	public String REQUEST_ROUTE="/requestRoute/requestRoute";
	public String REQUEST_ROUTES_LIST="/requestRoute/requestRoutesList";
	public String UPDATE_ROUTE="/persistRoute/updateRoute";
	
	//Sharedpreferences
	public String PREFS="clientPrefs";
	public String USER_LOGIN = "loginKey";
	public String USER_PASS = "passwordKey";
	public String USER_SESSINID = "sessionIDKey";
	
}
