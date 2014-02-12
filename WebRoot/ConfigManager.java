package com.wellsfargo.proxy;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;


/**
 * This class is an application configuration manager, containing shared global variables. 
 * It contains methods for handling values from property files, HTTP connection manager, 
 * and other application configurations. This class is intended as a singleton.
 *  
 * Properties files
 * proxy.properties: contains remote host/port, http connection, content/cookie/header filter settings
 * log4j.properties: contain log levels, log location 
 * killswitch.properties: killswitch for stopping all requests to Omniture 
 * 
 */
public class ConfigManager {
	/** Logger */
	private static final Logger log = Logger.getLogger(ConfigManager.class);

	/** ConfigManager */
	private static ConfigManager configManager;
	
	/** get an instance of Alarm */
	private static Alarm alarm = Alarm.getInstance();

	/** httpRetry is a setting for number of times to retry http connection */
	private int httpRetry;

	/** remoteHost is the destination hostname to forward requests */
	private String remoteHost;

	/** remotePort is the destination port to forward requests */
	private int remotePort = 80;

	/** remotePath is the path to append on the destination server */
	private String remotePath = "";

	/** internalProxyHost is the http proxy server */
	private String internalProxyHost = "";

	/** internalProxyPort is the http proxy port */
	private int internalProxyPort = 80;

	/** useInternalProxy is flag to use http proxy or not */
	private boolean useInternalProxy = false;

	/** useSSL is a flag to use http or https */
	private boolean useSSL = false;
	
	/** encodeCookies is a list of cookies to encode into location parameter */
	private String[] encodeCookies;
	
	/** encodeHeaders is a list of headers to encode into location parameter */
	private String[] encodeHeaders;	

	/** cookieWhiteList is a list of cookies that are allowed for request/response */
	private String[] cookieWhiteList;

	/** requestHeaderWhiteList is a list of HTTP header allowed for request */
	private String[] requestHeaderWhiteList;
	
	/** responseHeaderWhiteList is a list of HTTP header allowed for response */
	private String[] responseHeaderWhiteList;

	/** whiteListRegexPattern is a regular expression use to validate content format */
	private String whiteListRegexPattern = "";
	
	/** blackListRegexPattern is a list of blacklisted words which should be checked in the content */
	private String blackListRegexPattern;
	
	/** blackListExemptRegexPattern is a regular expression used for exemptions from the blacklist */
	private String blackListExemptRegexPattern;

	/** akamaiPattern is a regular expression used to remove the Akamai path */
	private String akamaiPattern;

	/** useFilterAkamai is a flag to use Akamai filter or not */
	private boolean useFilterAkamai;
	
	/**  */
	private String bundleName = "proxy";

	/**  */
	private ResourceBundle bundle = ResourceBundle.getBundle(bundleName);

	/** MultiThreadedHttpConnectionManager should be static, and passed to each HTTPClient instance.
	 *  Hence only one connection pool is created, and each HTTPClient instance reuses connections from the pool.  */
	private static MultiThreadedHttpConnectionManager connectionManager;

	/** timeOutInMillSecs */
	private int timeOutInMillSecs;
	
	/** maxTotalConnections */
	private int maxTotalConnections;
	
	

	/** ConfigManager private constructor */
	private ConfigManager() {
		initialize();
	}

	/**
	 * @return ConfigManager singleton
	 */
	public static ConfigManager getInstance() {
		if (configManager == null) {
			configManager = new ConfigManager();
		}
		return configManager;
	}


	/**
	 *  initialize method is called once when the class is constructed.
	 */
	private final void initialize() {
		if(log.isDebugEnabled()) {
			log.debug("Loading proxy.properties.");
		}
		
		try {
			httpRetry = getPropertyAsInt("httpRetry");
			remoteHost = getProperty("remoteHost");
			remotePort = getPropertyAsInt("remotePort");
			remotePath = getProperty("remotePath");
			internalProxyHost = getProperty("internalProxyHost");
			internalProxyPort = getPropertyAsInt("internalProxyPort");
			useInternalProxy = getPropertyAsBoolean("useInternalProxy");
			useSSL = getPropertyAsBoolean("useSSL");
			whiteListRegexPattern = getProperty("whiteListRegexPattern");
			blackListRegexPattern = getProperty("blackListRegexPattern");
			blackListExemptRegexPattern = getProperty("blackListExemptRegexPattern");
			encodeHeaders = getList("encodeHeaders");			
			encodeCookies = getList("encodeCookies");
			requestHeaderWhiteList = getList("requestHeaderWhiteList");
			responseHeaderWhiteList = getList("responseHeaderWhiteList");
			cookieWhiteList = getList("cookieWhiteList");
			akamaiPattern = getProperty("akamaiPattern");
			useFilterAkamai = getPropertyAsBoolean("useFilterAkamai");
			printProperties();

		} catch (MissingResourceException e) {
			log.error(e);
		}
		timeOutInMillSecs = getPropertyAsInt("timeOutInMillSecs");
		maxTotalConnections = getPropertyAsInt("maxTotalConnections");
		
		connectionManager = new MultiThreadedHttpConnectionManager();		
		connectionManager.getParams().setSoTimeout(timeOutInMillSecs);
		connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
		
		if(log.isDebugEnabled()) {
			log.debug("Connection Timeout set to " + timeOutInMillSecs);
			log.debug("Connection Pool Size set to " + maxTotalConnections);
		}		
	}

	
	/**
	 * 
	 * @return
	 */
	public boolean getKillSwitch() {
		PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration("killswitch.properties");			
			// This reloading strategy does not actively monitor a configuration file
			// It is triggered by its associated configuration whenever properties are accessed.
			FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
			int refreshDelay = Integer.parseInt(config.getString("refreshDelayInMillSecs"));
			if (refreshDelay > 0) {
				strategy.setRefreshDelay(refreshDelay);
			}
			else {
				strategy.setRefreshDelay(5000);
			}
			config.setReloadingStrategy(strategy); 

			
			if(log.isDebugEnabled()){
				log.debug("killSwitch : " + config.getString("killSwitch"));
			}
			return config.getString("killSwitch").equals("1");
		} catch (ConfigurationException e) {
			alarm.print("003", "CONFIGURATION ERROR", e);
		}
		return false;
	}

	/**
	 *  
	 */
	public void printProperties() {
		if(log.isDebugEnabled()){
			log.debug("httpRetry is : " + getHttpRetry());
			log.debug("remoteHost is : " + getRemoteHost());
			log.debug("remotePort is : " + getRemotePort());
			log.debug("remotePath is : " + getRemotePath());
			log.debug("internalProxyHost is : " + getInternalProxyHost());
			log.debug("internalProxyPort is : " + getInternalProxyPort());
			log.debug("useInternalProxy is : " + isUseInternalProxy());
			log.debug("useSSL is : " + isUseSSL());
			log.debug("timeOutInMillSecs is : " + timeOutInMillSecs);
			log.debug("whiteListRegexPattern is : " + whiteListRegexPattern);
			log.debug("blackListRegexPattern is : " + blackListRegexPattern);
			log.debug("blackListExemptRegexPattern is : " + blackListExemptRegexPattern);
			
			// print out string array values
			printList("encodeHeaders", getEncodeHeaders());
			printList("encodeCookies", getEncodeCookies());		
			printList("cookieWhiteList", getCookieWhiteList());
			printList("requestHeaderWhiteList", getRequestHeaderWhiteList());		
			printList("responseHeaderWhiteList", getResponseHeaderWhiteList());
		}		
	}

	/**
	 * @param name
	 * @param list
	 */
	private void printList(String name, String[] list) {
		StringBuffer printlist = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				printlist.append(", " + list[i]);
			} else {
				printlist.append(list[i]);
			}
		}
		log.debug(name + " is : " + printlist.toString());		
	}
	
	/**
	 * @return
	 */
	public String getWhiteListRegexPattern() {
		return whiteListRegexPattern;
	}
	
	/**
	 * @return
	 */
	public String getBlackListRegexPattern() {
		return blackListRegexPattern;
	}

	/**
	 * 
	 * @return
	 */
	public String getBlackListExemptRegexPattern() {
		return blackListExemptRegexPattern;
	}
	
	/**
	 * @return
	 */
	public String[] getRequestHeaderWhiteList() {
		return requestHeaderWhiteList;
	}

	
	/**
	 * @return
	 */
	public String[] getResponseHeaderWhiteList() {
		return responseHeaderWhiteList;
	}

	/**
	 * @return
	 */
	public String[] getCookieWhiteList() {
		return cookieWhiteList;
	}

	/**
	 * @return
	 */
	public String[] getEncodeHeaders() {
		return encodeHeaders;
	}
	
	
	/**
	 * @return
	 */
	public String[] getEncodeCookies() {
		return encodeCookies;
	}

	/**
	 * @return
	 */
	public int getHttpRetry() {
		return httpRetry;
	}

	/**
	 * @return
	 */
	public String getInternalProxyHost() {
		return internalProxyHost;
	}

	/**
	 * @return
	 */
	public int getInternalProxyPort() {
		return internalProxyPort;
	}

	/**
	 * @return
	 */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @return
	 */
	public String getRemotePath() {
		return remotePath;
	}

	/**
	 * @return
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @return
	 */
	public boolean isUseInternalProxy() {
		return useInternalProxy;
	}

	/**
	 * @return
	 */
	public boolean isUseSSL() {
		return useSSL;
	}

	
	
	/**
	 * @param prop
	 * @return
	 */
	private String getProperty(String prop) {
		try {
			return bundle.getString(prop);
		} catch (MissingResourceException e) {
			log.error(e);
		}
		return "";
	}

	/**
	 * @param prop
	 * @return
	 */
	private boolean getPropertyAsBoolean(String prop) {
		try {
			return (bundle.getString(prop).equals("1"));
		} catch (MissingResourceException e) {
			log.error(e);
		}
		return false;
	}

	/**
	 * @param prop
	 * @return
	 */
	private int getPropertyAsInt(String prop) {
		try {
			return (Integer.parseInt(bundle.getString(prop)));
		} catch (MissingResourceException e) {
			log.error(e);
		}
		return -1;
	}

	/**
	 * @param prop
	 * @return
	 */
	private String[] getList(String prop) {
		try {
			String list = bundle.getString(prop);
			String[] l = list.split(","); // comma delimiter
			return l;
		} catch (MissingResourceException e) {
			log.error(e);
		}
		return null;
	}



	/**
	 * 
	 * @return
	 */
	public String getAkamaiPattern() {
		return akamaiPattern;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isUseFilterAkamai() {
		return useFilterAkamai;
	}
	
	/**
	 * 
	 * @return
	 */
	public static MultiThreadedHttpConnectionManager getConnectionManager() {
		return connectionManager;
	}
	
}
