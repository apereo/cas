/*
 * @(#)HttpsUtil.java $version 2015. 1. 23.
 *
 * Copyright 2014 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jasig.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.servlet.http.Cookie;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author ruaa
 * @since 2015. 1. 23.
 */
public class HttpTemplate {

	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final String PATH = "path";
	private static final String DOMAIN = "domain";
	private static final char NAME_VALUE_SEPARATOR = '=';

	/** Logger instance. **/
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public <T> HttpResponse<T> request(String strUrl
			, String method
			, Map<String, String> header
			, ResponseReadCallback<T> responseReadCallback) {
		return request(strUrl, method, header, null, responseReadCallback, null, false);
	}

	public <T, V> HttpResponse<V> request(String strUrl
			, String method
			, Map<String, String> header
			, BodyWriteCallback<T> bodyWriteCallback
			, ResponseReadCallback<V> responseReadCallback, T body) {
		return request(strUrl, method, header, bodyWriteCallback, responseReadCallback, body, false);
	}
	
	public <T, V> HttpResponse<V> request(String strUrl
			, String method
			, Map<String, String> header
			, BodyWriteCallback<T> bodyWriteCallback
			, ResponseReadCallback<V> responseReadCallback, T body, boolean isSelfSigned) {
		
		HttpResponse<V> response;
		try {
			URL url = new URL(strUrl);

			HttpURLConnection con;
			boolean isSsl = strUrl.toLowerCase().indexOf("https") >= 0 ? true: false;
			if(isSsl) {
				con = (HttpsURLConnection) url.openConnection();
			} else {
				con = (HttpURLConnection) url.openConnection();
			}

			if (isSelfSigned) {
				try {
					setAcceptUntrustedCert((HttpsURLConnection)con);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			con.setRequestMethod(method);
			con.setDoOutput(true);

			if (header != null) {
				for (Entry<String, String> entry : header.entrySet()) {
					con.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			if(body != null) {
				bodyWriteCallback.write(con, body);
			}
			
			response = new HttpResponse<V>();
			response.setResponseCode(con.getResponseCode());
			response.setResponseBody(responseReadCallback.read(con.getInputStream()));
			response.setCookieList(extractCookie(con));

			con.disconnect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return response;
	}

	private List<Cookie> extractCookie(HttpURLConnection connection) {
		List<Cookie> cookieList = new ArrayList<>();

		String headerName = null;
		for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equalsIgnoreCase(SET_COOKIE)) {
				String[] cookieInfoSet = connection.getHeaderField(i).split(COOKIE_VALUE_DELIMITER);

				String cookieInfo = cookieInfoSet[0];
				String cookieName = cookieInfo.substring(0, cookieInfo.indexOf(NAME_VALUE_SEPARATOR));
				String cookieValue = cookieInfo.substring(cookieInfo.indexOf(NAME_VALUE_SEPARATOR) + 1);

				logger.info(String.format("cookie: name=%s, value=%s", cookieName, cookieValue));
				Cookie cookie = new Cookie(cookieName, cookieValue);

				// 해당 쿠키의 메타데이터 추출
				int cookieInfoLength;
				String metaInfo = null;
				if ((cookieInfoLength = cookieInfoSet.length) > 1) {
					for (int index = 1; index < cookieInfoLength; index++) {
						metaInfo = cookieInfoSet[index];
						String metaName = metaInfo.substring(0, metaInfo.indexOf(NAME_VALUE_SEPARATOR)).trim();
						String metaValue = metaInfo.substring(metaInfo.indexOf(NAME_VALUE_SEPARATOR) + 1);

						logger.info(String.format("cookie meta: name=%s, value=%s", metaName, metaValue));

						switch (metaName.toLowerCase()) {
							case PATH:
								cookie.setPath(metaValue);
								break;
							case DOMAIN:
								cookie.setDomain(metaValue);
								break;
						}
					}
				}

				cookieList.add(cookie);
			}
		}

		return cookieList;
	}

	private void setAcceptUntrustedCert(HttpsURLConnection connection) throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

					public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

				}
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		connection.setSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		connection.setHostnameVerifier(allHostsValid);
	}

	public interface BodyWriteCallback<T> {
		void write(HttpURLConnection conn, T body);
	}
	
	public interface ResponseReadCallback<T> {
		T read(InputStream br);
	}
}
