package com.wellsfargo.proxy;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * OmnitureRequest builds the HTTP request object that is used for Omniture calls.
 * The class builds an GetMethod so that OmnitureService can focus on executing the call.
 *
 * Headers are transferred from original request to the service request, filtered based on
 * the request/response header whitelist.
 *
 * It also encodes the cookie and header information to the location parameter.
 */
public class OmnitureRequest {

	/**  */
	private static final Log log = LogFactory.getLog(OmnitureRequest.class);

	private static final String COOKIE_HEADER = "Cookie";

	private static final String SIMSCOOKIE = "SIMSCookie";
	
	/**  */
	private static ConfigManager config = ConfigManager.getInstance();

	private static Filter filter = Filter.getInstance();

	/**  */
	private GetMethod method;

	/**  */
	private String pageLocation = "";

	/**  */
	private String headerParameters = "";

	private String url = "";

	/**
	 * Constructor
	 * @param request
	 */
	public OmnitureRequest(HttpServletRequest request) {
		try {
			createRequest(request);
		} catch (Exception e) {
			if(log.isErrorEnabled()) {
				log.error("err_code:ERR_PROXYSERVER_006~msg:REQUEST DATA ERROR.  Request data missing " +
						"or un-defined. - Request: " + getUrl());
			}
		}
	}

	/**
	 * createRequest builds the request with URL and headers
	 * Headers are transferred from original request to the service request.
	 * @param request
	 * @throws Exception
	 */
	private final void createRequest(HttpServletRequest request) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("========================= OmnitureRequest - start =========================");
			log.debug("");
			log.debug("ServletPath : " + request.getServletPath());
			Enumeration params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String param = (String) params.nextElement();
				String paramValue = request.getParameter(param);
				log.debug("Request parameter : " + param + "=" + paramValue);
			}

		}

		// instantiate the method
		method = new GetMethod();

		// Transfer the headers from the request received to the httpclient
		// request.

		StringBuffer encodedHeaders = new StringBuffer();

		Enumeration headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String header = (String) headers.nextElement();
			String headerValue = request.getHeader(header);

			// Check if each header matches one of encoded headers
			if (filter.isInEncodeHeaders(header)) {
				// Building out the header name=value& string.
				encodedHeaders.append("&" + header + "=" + headerValue);
				log.debug("Encoding Header to URL: " + "&" + header + "=" + headerValue);
			}

			if (filter.isInRequestHeaderWhiteList(header)) {
				if (!headerValue.equals("")) {
					method.addRequestHeader(header,headerValue);
					if (log.isDebugEnabled()) {
						log.debug("Adding Header to Request: " + header + " : "	+ headerValue);
					}
				}
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Not Adding Header to Request: " + header + " : " + headerValue);
				}
			}

			if (header.equalsIgnoreCase(COOKIE_HEADER)) {
				long start1 = System.currentTimeMillis();

				method.addRequestHeader(header, filterCookiesInRequest(request));

				long end1 = System.currentTimeMillis();

				if(log.isInfoEnabled()) {
				   log.info("");
				   log.info("Cookie Filter Milliseconds : " + (end1 - start1));
				   log.info("");
				}


			}

		}

		headerParameters = encodedHeaders.toString();

		// If there are cookies, then trim the extra &, and encode the
		// string.
		if (headerParameters.length() > 1) {
			try {
				headerParameters = URLEncoder.encode(headerParameters,"UTF-8");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		log.debug("Created headerParameters : " + headerParameters);

			// Load Configuration Manager for base url, etc
			url = this.buildRequestURL(request);
			method.setURI(new URI(url,true));

			if (log.isInfoEnabled()) {
			   log.info("Adding URL to Request : " + url);
			}

		// Provide custom retry handler is necessary
		method.getParams().setParameter(
			HttpMethodParams.RETRY_HANDLER,
			new DefaultHttpMethodRetryHandler(
				config.getHttpRetry(), false));
		if (log.isDebugEnabled()) {
			log.debug("Setting GetMethod Retry attempts : " + config.getHttpRetry());
		}

	}

	/**
	 * Filter out all cookies EXCEPT for the cookies in the whitelist.
	 * @param request
	 * @return myCookies
	 */
	public String filterCookiesInRequest(HttpServletRequest request)throws Exception {
		Cookie[] cookies = request.getCookies();
		String myCookies = "";
		if (log.isDebugEnabled()) {
			log.debug("Cookies (pre-filter) : " + request.getHeader(COOKIE_HEADER));
		}

		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (log.isDebugEnabled()) {
					log.debug("Evaluating Cookie for Request : " + cookies[i].getName()
							+ "=" + cookies[i].getValue() + " domain=" + cookies[i].getDomain());
				}

				if (Filter.getInstance().isInCookieWhiteList(
						cookies[i].getName())) {
					myCookies += " " + cookies[i].getName() + "="
							+ cookies[i].getValue() + ";";
					if (log.isDebugEnabled()) {
						log.debug("Adding Cookie to Request : " + cookies[i].getName()
								+ "=" + cookies[i].getValue());
					}

				}
			}
			// trim trailing ; from string
			int l = myCookies.length();
			if (l > 0 && myCookies.substring(l - 1).equals(";")) {
				myCookies = myCookies.substring(0, l - 1);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Cookies (post-filter) : " + myCookies);
		}

		return myCookies;
	}

	/**
	 * The buildRequestURL String method goes through the parameters names and
	 * finds the location parameter. It then adds the buildCookiesURL value
	 * (cookieParameters) to the location parameter and encodes the entire
	 * location value.
	 *
	 * @param request
	 * @return URL
	 */

	private String buildRequestURL(HttpServletRequest request) throws Exception {
		String URL = "";
		// use http or https based on useSSL property
		if (config.isUseSSL()) {
			URL = config.getRemoteHostSSL();
		} else {
			URL = config.getRemoteHost();
		}
		if (log.isDebugEnabled()) {
			log.debug("Building URL.");
			log.debug("Server and Port : " + URL);
		}



		// if servlet mapping url-pattern in web.xml is /c and /i
		if (request.getServletPath() != null) {
			URL += request.getServletPath();
			if (log.isDebugEnabled()) {
				log.debug("Adding ServletPath : " + request.getServletPath());
			}
		}


		if (log.isDebugEnabled()) {
			log.debug("BaseUrl : " + URL);
		}

		URL = this.addQueryString(URL, request);

		return URL;
	}

	/**
	 * AddQueryString builds the query string from cookie parameters
	 * @param url
	 * @param request
	 * @return queryString
	 */
	private String addQueryString(String url, HttpServletRequest request) throws Exception {
		String cookieParams = buildCookieString(request);
		if (log.isDebugEnabled()) {
			log.debug("Created cookieParams : " + cookieParams);
		}
		String queryString = addCookieParams(url, request, cookieParams);

		return queryString;
	}

	/**
	 *  This builds the encoded cookieParameters for location query string parameter.
	 * @param request
	 * @return cookieParameters + headerParameters;
	 */
	private String buildCookieString(HttpServletRequest request)
	throws Exception {

		String cookieParameters = new String();
		Cookie[] cookies = request.getCookies();

		StringBuffer url = new StringBuffer();
		int cookieCount = 0;
		boolean cookieFound = false;
		if (cookies != null) {
			// Go through the cookies
			for (int i = 0; i < cookies.length; i++) {
				String cookieName = cookies[i].getName();
				String cookieValue = cookies[i].getValue();
				// Check if each cookie matches one of the allowed cookies
				for (int j = 0; j < config.getEncodeCookies().length; j++) {
					String cookiesForUrlName = config.getEncodeCookies()[j];
					// Building out the cookie name=value& string.
					if (cookieName.equals(cookiesForUrlName)) {
						if (cookieCount > 0) {
							url.append("&" + cookieName + "=" + cookieValue);
							log.debug("Encoding Cookie to URL: " + "&" + cookieName + "=" + cookieValue);
						} else {
							url.append(cookieName + "=" + cookieValue);
							log.debug("Encoding Cookie to URL: " + cookieName + "=" + cookieValue);
							cookieCount++;
						}
					}

				}
				// Check for SIMSCookie
				if (cookieName.equals(SIMSCOOKIE)){
					log.debug("SIMSCookie Found. " + SIMSCOOKIE + "=1");
					url.append("&" + SIMSCOOKIE + "=1");
					cookieFound = true;
				}
			}
			if (!cookieFound) {
				log.debug("SIMSCookie Not Found. " + SIMSCOOKIE + "=0");
				url.append("&" + SIMSCOOKIE + "=0");
			}

			cookieParameters = url.toString();

			// If there are cookies, then trim the extra &, and encode the
			// string.
			if (cookieParameters.length() > 1) {
				//cookieParameters = cookieParameters.substring(0, cookieParameters.length() - 1);
				try {
					cookieParameters = URLEncoder.encode(cookieParameters,
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

			}

		}
		// Return cookieParameters. Will be the result of the functions or blank
		// ("").
		return cookieParameters + headerParameters;
	}

	/**
	 * The addCookieParams String method goes through the parameters names and
	 * finds the location parameter. It then adds the buildCookiesURL value
	 * (cookiePramas) to the location parameter and encodes the entire location
	 * value. It then adds all the parameters back the baseURL.
	 *
	 * @param baseURL
	 * @param request
	 * @param cookiesParams
	 * @return URL
	 */
	private String addCookieParams(String baseURL, HttpServletRequest request,
			String cookiesParams) throws Exception {

		if (cookiesParams.equals("")) {
			pageLocation = request.getQueryString();
			return baseURL + "?" + request.getQueryString();

		} else {
			String[] URLparts = request.getQueryString().split("&");
			StringBuffer newURL = new StringBuffer();
			String delimiter = "";

			for (int j = 0; j < URLparts.length; j++) {
				if (j > 0) {
					delimiter = "&";
				}
				if (URLparts[j].startsWith("location")) {
					pageLocation = URLparts[j] + "%26" + cookiesParams;
					newURL.append(delimiter + pageLocation);
				} else {
					// construct query string addition
					newURL.append(delimiter + URLparts[j]);
				}
			}
			return baseURL + "?" + newURL.toString();
		}

	}

	/**
	 *
	 * @return
	 */
	public GetMethod getMethod() {
		return method;
	}

	/**
	 *
	 * @param method
	 */
	public void setMethod(GetMethod method) {
		this.method = method;
	}


	public String getPageLocation() {
		//System.out.println("pageLocation is " + pageLocation);
		return pageLocation;
	}

	public String getUrl() {
		return url;
	}


}
