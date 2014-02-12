package com.wellsfargo.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OmnitureService will make a request using the OmnitureRequest object.
 * OmnitureService upon successful connection, creates an OmnitureResponse
 * object and provides getMethod for Delegate to get OmnitureResponse.
 * 
 */
public class OmnitureService {

	/** */
	private static final Log log = LogFactory.getLog(OmnitureService.class);
	
	/** */
	private OmnitureResponse omnitureResponse;
	
	/** get an instance of ConfigManager */
	private static ConfigManager config = ConfigManager.getInstance();

	/**
	 * Constructor
	 * 
	 * @param omnitureRequest
	 */
	OmnitureService(OmnitureRequest omnitureRequest) {
		this.omnitureResponse = new OmnitureResponse();
		makeRequest(omnitureRequest);
	}

	/**
	 * This makes the request to Omniture, and filters the response by header,
	 * cookies and HTTP status code.
	 * 
	 * @param omnitureRequest
	 */
	public final void makeRequest(OmnitureRequest omnitureRequest) {
		if (log.isDebugEnabled()) {
			log.debug("");
			log
					.debug("========================= OmnitureService =========================");
			log.debug("");
		}

		HttpClient client = new HttpClient();
		client.getParams().setSoTimeout(config.getTimeOutInMillSecs());
		
		HttpClientParams httpClientParams = client.getParams();
		httpClientParams.setVirtualHost(config.getVirtualHost());
		
		 if (config.isUseInternalProxy()) {
	        	client.getHostConfiguration().setProxy(config.getInternalProxyHost(), config.getInternalProxyPort());
	        }		
		
		
		GetMethod method = omnitureRequest.getMethod();
		
		int statusCode = 0;
		String body = "";
		BufferedReader bufferedReader;

		try {
			if (log.isDebugEnabled()) {
				log.debug("Make Connection.");
			}

			long start = System.currentTimeMillis();
			statusCode = client.executeMethod(method);
			long end = System.currentTimeMillis();
			if (log.isInfoEnabled()) {
				log.info("");
				log.info("Omniture Request/Response Milliseconds : "
						+ (end - start));
				log.info("");
			}
			
			bufferedReader = new BufferedReader(new InputStreamReader(
					method.getResponseBodyAsStream(), method.getResponseCharSet())); 
			
			
			body = bufferedReader.readLine();

		} catch (HttpException httpe) {
			
			if(log.isErrorEnabled()) {
				log.error("err_code:ERR_PROXYSERVER_001~msg:HTTP CONNECTION ERROR -  *Request: " + omnitureRequest.getUrl());
			}
			
		} catch (IOException ioe) {
			
			if(log.isErrorEnabled()) {
				log.error("err_code:ERR_PROXYSERVER_002~msg:SOCKET ERROR CONNECTION EXCEEDED "+ config.getTimeOutInMillSecs()+" MILLISECONDS TIMEOUT - *Request: " + omnitureRequest.getUrl());
			}
			
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		

		// if not HTTP 200, then return blank response
		if (log.isInfoEnabled()) {
			log.info("Received HTTP Response Code: " + statusCode);
		}

		if (statusCode >= 400) {
			
			if(log.isErrorEnabled()) {
				log.error("err_code:ERR_PROXYSERVER_005~msg:OMNITURE RESPONSE ERROR.  Response received from " +
						"Omniture but is not 200 status. -  *Request: " + omnitureRequest.getUrl()  + 
						"  *Response: " + body);
			}
			
			body = "";
		}

		// filtering the http response from server to client response
		if (!body.equals("")) {
			long start1 = System.currentTimeMillis();

			String errorDescr;
			errorDescr = Filter.getInstance().doFilter(body);

			if (errorDescr.equals("pass")) {
				body = Filter.getInstance().akamaiFilter(body);
			} else {
				
				if(log.isErrorEnabled()) {
					log.error("err_code:ERR_PROXYSERVER_004~msg:FILTER RESPONSE ERROR.  The Content filter " +
							"has detected am error. -   *Request: " + omnitureRequest.getUrl()  + 
							"   *Response: " + body + "   *Error Description: " + errorDescr);
				}
				
				body = "";
			}

			long end1 = System.currentTimeMillis();

			if (log.isInfoEnabled()) {
				log.info("");
				log.info("Content Filter Milliseconds : " + (end1 - start1));
				log.info("");
			}

		}
		if (log.isDebugEnabled()) {
			log.debug("Post-Filter Content: " + body);
		}

		omnitureResponse.setContent(body);
		omnitureResponse.setHeaders(method.getResponseHeaders());

		omnitureResponse.setCookies(client.getState().getCookies());

	}

	/**
	 * @return
	 */
	public OmnitureResponse getOmnitureResponse() {
		return omnitureResponse;
	}

}