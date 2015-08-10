package pl.pap.utils;



public interface Consts {
	// /@class path/@method path
	public String domainAdress="http://192.168.88.252:10080/PAPserver";
	//public String domainAdress="http://192.168.43.254:10080/PAPserver";
	public String LOGIN="/login/dologin";
	public String REGISTER="/register/doregister";
	public String GET_USER_INFO="/userinfo/getinfo";
	public String PERSIST_ROUTE="/persistRoute/persistRoute";
	public String REQUEST_ROUTE="/requestRoute/requestRoute";
	public String REQUEST_ROUTES_LIST="/requestRoute/requestRoutesList";
	public String UPDATE_ROUTE="/persistRoute/updateRoute";
	public String DELETE_ROUTE="/deleteRoute/deleteRoute";
	
	//Sharedpreferences
	public String PREFS="clientPrefs";
	public String USER_LOGIN = "loginKey";
	public String USER_PASS = "passwordKey";
	public String USER_SESSINID = "sessionIDKey";
	//Offline Mode
	public String ROUTE_KEY="route";
	public int ROUTE_MEMORY_SPACE = 10;
	
	//Location
	public static int UPDATE_INTERVAL = 10000; // 10 sec
	public static int FASTEST_INTERVAL = 5000; // 5 sec
	public static int DISPLACEMENT = 10; // 10 meters
	public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	
	//REST api
	
	
}
