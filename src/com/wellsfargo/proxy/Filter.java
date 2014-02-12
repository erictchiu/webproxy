package com.wellsfargo.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter contains methods for filtering HTTP request/response, including content, 
 * cookie and HTTP headers. This class is intended as a singleton.
 */
public class Filter {
	/** */
	private static final Log log = LogFactory.getLog(Filter.class);

	/** */
	private static ConfigManager config = ConfigManager.getInstance();
	
	/** */
	private static Filter filter;

	/** */
	private Filter() {
	}

	/**
	 * Constructor for Filter class
	 * @return instance of Filter
	 */
	public static Filter getInstance() {
		if (filter == null) {
			filter = new Filter();
		}
		return filter;
	}


	/**
	 * Checks for whether header is in the encodeHeaders list or not. 
	 * Return true if header is in the encodeHeaders list.
	 * @param headerName
	 * @return boolean
	 */
	public boolean isInEncodeHeaders(String headerName) {
		for (int i = 0; i < config.getEncodeHeaders().length; i++) {
			if (config.getEncodeHeaders()[i].equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}		
	
	/**
	 * Checks for whether header is in the whitelist or not. 
	 * Return true if header is in the requestHeaderWhiteList.
	 * 
	 * @param headerName
	 * @return boolean
	 */
	public boolean isInRequestHeaderWhiteList(String headerName) {
		for (int i = 0; i < config.getRequestHeaderWhiteList().length; i++) {
			if (config.getRequestHeaderWhiteList()[i].equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}	

	/**
	 * Checks for whether header is in the whitelist or not. 
	 * Return true if header is in the responseHeaderWhiteList.
	 * 
	 * @param headerName
	 * @return boolean
	 */
	public boolean isInResponseHeaderWhiteList(String headerName) {
		for (int i = 0; i < config.getResponseHeaderWhiteList().length; i++) {
			if (config.getResponseHeaderWhiteList()[i].equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}		
	
	
	
	/** 
	 * perform cookie filtering against cookiesWhitelist
	 * @param cookies
	 * @param response
	 */
	public void filterCookiesInResponse(Cookie[] cookies,
			HttpServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("");
			log.debug("  =============   Filter ================");
			log.debug("filterCookiesInResponse.");
		}

		String myCookies = "";
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (isInCookieWhiteList(cookie.getName())) {

				if (log.isDebugEnabled()) {
					log.debug("Adding Cookie to Proxy Response : "
							+ cookie.getName() + " = " + cookie.getValue());
				}

				response.addCookie(new javax.servlet.http.Cookie(cookie
						.getName(), cookie.getValue()));
				myCookies += cookie.getName() + " = " + cookie.getValue()
						+ "; ";
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Cookies (post-filter) :" + myCookies);
		}

	}


	/**
	 * Checks for whether cookie value is in the white list or not. 
	 * Return true if cookie is in the whitelist.
	 * 
	 * @param cookieValue
	 * @return boolean
	 */
	public boolean isInCookieWhiteList(String cookieValue) {
		String cookieName = cookieValue.split(";")[0].trim().split("=")[0].trim();
		for (int i = 0; i < config.getCookieWhiteList().length; i++) {			
			if (config.getCookieWhiteList()[i].equals(cookieName) || cookieName.startsWith("NSC_")) {
				return true;
			}			
		}
		return false;
	}

	/**
	 * If content blacklist string is detected, return empty string. 
	 * This results in a blank HTTP response.
	 * 
	 * @param body
	 * @return String
	 */
	public String doFilter(String body) {
		String result = "pass";
		if (log.isDebugEnabled()) {
			log.debug("");
			log.debug("  =============  Filter ================");
			log.debug("doFilter received content");
		}
		
		 result = whiteListError(body);
         if (result.equals("pass")) {
			result = blackListError(body);
		}
		
		return result;
	}

	
	
	public String akamaiFilter(String body) {
		if (log.isDebugEnabled()) {
			log.debug("");
			log.debug("akamai check");
		}
		
		//remove all Akamai references from body
		if (config.isUseFilterAkamai()) {
			Pattern p = Pattern.compile(config.getAkamaiPattern());
			Matcher m = p.matcher(body);
			body = m.replaceAll("");
		}
		return body;
	}
	
	
	/**
	 * Checks for whether string contains a blacklisted word or not. 
	 * Return true if a blacklist word is detected. 
	 * @param body
	 * @return boolean
	 */
	private static String blackListError(String body) {
		String result = "pass";
		String content = body.toLowerCase(); // make this filter case insensitive

		String blacklist = config.getBlackListRegexPattern();
		String exempt = config.getBlackListExemptRegexPattern();
		
		Pattern pattern = Pattern.compile(blacklist + "|" + exempt);
		Matcher matcher = pattern.matcher(content);
		
		if (matcher.find()) {
			try {
			
				result = "Found blacklisted word \"" + matcher.group() + "\" at position " + matcher.start() + ".";
		
				
			} catch (StringIndexOutOfBoundsException e) {
				log.error("Could not get pre-word context.");
			}

		}

		return result;
	}

	private static String whiteListError (String body) {
		String result;
	    	
    	String whitelist = config.getWhiteListRegexPattern();
    	
		String content = body.toLowerCase(); // make this filter case insensitive
		
		Pattern pattern = Pattern.compile(whitelist);
		Matcher matcher = pattern.matcher(content);					

		if (matcher.find()) {
			log.debug("Passed whitelist.");
			result = "pass";
		}
		else {
			result = "Does not match whitelist pattern.";	
		}
		
		return result;
	}
	
}
