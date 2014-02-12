package com.wellsfargo.proxy;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ProxyServlet is used as a front controller for Omniture request/response.
 * Most of the processing is done by helper classes that it calls directly or indirectly.
 *   
 * It creates an OmnitureRequest, passes it to the OmnitureDelegate, which creates 
 * OmnitureResponse. 
 */
public class ProxyServlet extends HttpServlet {

	/**  */
	private static final Log log = LogFactory.getLog(ProxyServlet.class);
	/**  */
	private static ConfigManager config;
	/**  */
	private static Filter filter;
	
	private static final long serialVersionUID = 1L;


	/* (non-Javadoc)
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 * Initializes the configuration and filter. 
	 */
	public void init(ServletConfig servletConfig) {
		if(log.isInfoEnabled()) {
			log.info("");
			log.info("  =========     ProxyServlet init    ===================     ");
			log.info("");
		}
				
		config = ConfigManager.getInstance();
		filter = Filter.getInstance();
	
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * Handles HTTP GET requests from web clients to the ProxyServlet. Writes the response.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		long start = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("");
			log.info("");
			log.info("  =========  START ===================     ");
			log.info("");
		}
		
		String content;
		Header[] omnitureHeaders;
		OmnitureRequest omnitureRequest;
		OmnitureService omnitureService;
		OmnitureResponse omnitureResponse;
		

		// Create an OmnitureRequest object with HttpServletRequest object.
		omnitureRequest = new OmnitureRequest(request);

		// Get the response ready
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();

		// if true, do nothing. empty response is returned to client, thread is
		// released.
		if (!config.getKillSwitch()) {

			omnitureService = new OmnitureService(omnitureRequest); 
			omnitureResponse = omnitureService.getOmnitureResponse();
            
			
			if(log.isDebugEnabled()) {
				log.debug("");  
				log.debug("  =========  ProxyServlet response  ============   ");
			}
			
			content = omnitureResponse.getContent();
			omnitureHeaders = omnitureResponse.getHeaders();
			if (omnitureHeaders != null) {
				
				for (int i = 0; i < omnitureHeaders.length; i++) {
					if (filter.isInResponseHeaderWhiteList(omnitureHeaders[i].getName())) { 					
						response.setHeader(omnitureHeaders[i].getName(),
								omnitureHeaders[i].getValue());
						if(log.isDebugEnabled()) {
							log.debug("Adding Header to Proxy Response : " + omnitureHeaders[i].getName()+ " : " + 
									omnitureHeaders[i].getValue());
						}
					}	
					else {	
						if(log.isDebugEnabled()) {
							log.debug("Not Adding Header to Proxy Response : " + omnitureHeaders[i].getName()+ " : " + 
									omnitureHeaders[i].getValue());
						}						
					}
				}	
				
			} 
			else {
				
				log.error("Omniture is not sending Headers");
			}
			

			// filter and add cookies
			long start1 = System.currentTimeMillis();
			
			filter.filterCookiesInResponse(omnitureResponse.getCookies(),
					response);
			
			long end1 = System.currentTimeMillis();
			
			if(log.isInfoEnabled()) {
			   log.info("");
			   log.info("Cookie Filter Milliseconds : " + (end1 - start1));
			   log.info("");
			}


			if(log.isInfoEnabled()) {
				log.info("Response Content: " + content);
			}
			
			pw.println(content);

		} else {
			log.error("Kill Switch is ENABLED.");
			pw.println("");
		}

		pw.close();
		
		long end = System.currentTimeMillis();
		if(log.isInfoEnabled()) {
			log.info("");
			log.info("Total Processing Milliseconds : " + (end - start));
			log.info("");
			log.info("  =========  END ===================     ");
			log.info("");
			log.info("");
		}		
		
	}

}
