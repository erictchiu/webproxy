package com.wellsfargo.proxy;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;


	/**
	 * Simple ValueObject based on the HttpClient response objects
	 * that come back from and Omniture HttpRequest.
	 *
	 */
	public class OmnitureResponse {
		
		/**  */
		private Header[] headers;
		/**  */
		private String content;
		/**  */
		private Cookie[] cookies;
		
		private static String noResponse = "";
		
		/**
		 * @return blank response
		 */
		public static String getNoResponse() {
			return noResponse;
		}		

		/**
		 * @return
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content
		 */
		public void setContent(String content) {
			this.content = content;
		}


		/**
		 * @return
		 */
		public Header[] getHeaders() {
			return headers;
		}

		/**
		 * @param headers
		 */
		public void setHeaders(Header[] headers) {			
			this.headers = headers;
		}

		/**
		 * @return
		 */
		public Cookie[] getCookies() {
			return cookies;
		}
		
		/**
		 * @param cookies
		 */
		public void setCookies(Cookie[] cookies) {
			this.cookies = cookies;
		}		
		
	}
