package com.authy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to send http requests.
 * @author Julian Camargo
 *
 */
public class Resource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String apiUri, apiKey;
	private int status;
	private boolean testFlag = false;

	public static final String ENCODE = "UTF-8";
	public static final String XML_CONTENT_TYPE = "application/xml";
	
	public Resource(String uri, String key) {
		apiUri = uri;
		apiKey = key;
	}
	
	public Resource(String uri, String key, boolean testFlag) {
		apiUri = uri;
		apiKey = key;
		this.testFlag = testFlag;
	}
	
	/**
	 * POST method.
	 * @param path the path
	 * @param data the data
	 * @return response from API.
	 */
	public String post(String path, Response data) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("Content-Type", XML_CONTENT_TYPE);

		return request("POST", path, data, options);
	}
	
	/**
	 * GET method.
	 * @param path the path
	 * @param data the data
	 * @return response from API.
	 */
	public String get(String path, Response data) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("Content-Type", XML_CONTENT_TYPE);

		return request("GET", path, data, options);
	}
	
	/**
	 * PUT method.
	 * @param path the path
	 * @param data the data
	 * @return response from API.
	 */
	public String put(String path, Response data) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("Content-Type", XML_CONTENT_TYPE);

		return request("PUT", path, data, options);
	}
	
	/**
	 * DELETE method.
	 * @param path the path
	 * @param data the data
	 * @return response from API.
	 */
	public String delete(String path, Response data) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("Content-Type", XML_CONTENT_TYPE);

		return request("DELETE", path, data, options);
	}
	
	public String request(String method, String path, Response data, Map<String, String> options) {
		HttpURLConnection connection = null;
		String answer = null;
		
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("?api_key=" + apiKey);
			
			if(method.equals("GET")) {
				sb.append(prepareGet(data));
			}
			
			URL url = new URL(apiUri + path + sb.toString());
			connection = createConnection(url, method, options);
			
			if(method.equals("POST") || method.equals("PUT")) {
				writeXml(connection, data);
			}

			status = connection.getResponseCode();
			answer = getResponse(connection);
		}
		catch(SSLHandshakeException e) {
			System.err.println("SSL verification is failing. This might be because of an attack. Contact support@authy.com");
		}
		catch(Exception e) {
            logger.error(e.getMessage(), e);
		}
		return answer;
	}

	/**
	 * Get status from response.
	 * @return status code.
	 */
	public int getStatus() {
		return status;
	}

    public void setStatus(int s) {
        this.status = s;
    }

	protected HttpURLConnection createConnection(URL url, String method, 
			Map<String, String> options) throws Exception {

		
		HttpURLConnection connection = null;
		if(testFlag)
			connection = (HttpURLConnection)url.openConnection();
		else
			connection = (HttpsURLConnection)url.openConnection();
		
		connection.setRequestMethod(method);
		
		for(Entry<String, String> s : options.entrySet()) {
			connection.setRequestProperty(s.getKey(), s.getValue());
		}

		connection.setDoOutput(true);
		
		return connection;
	}
	
	private String getResponse(HttpURLConnection connection) throws Exception {
		InputStream in = null;
		// Load stream
		if(status != 200) {
			in = connection.getErrorStream();
		}
		else {
			in = connection.getInputStream();
		}
		
		BufferedInputStream input = new BufferedInputStream(in);
		StringBuffer sb = new StringBuffer();
		int ch;
		
		while((ch = input.read()) != -1) {
			sb.append((char)ch);
		}
		input.close();

		return sb.toString();
	}
	
	private void writeXml(HttpURLConnection connection, Response data) throws SSLHandshakeException, IOException {
		if(data == null)
			return;
		
		OutputStream os = connection.getOutputStream();
		
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(os));
		output.write(data.toXML());
		output.flush();
		output.close();
	}
	
	private String prepareGet(Response data) throws Exception {
		if(data == null)
			return "";
		
		StringBuffer sb = new StringBuffer();
		Map<String, String> params = data.toMap();

		for(Entry<String, String> s : params.entrySet()) {
			sb.append('&');
			sb.append(URLEncoder.encode(s.getKey(), ENCODE) + "=" + URLEncoder.encode(s.getValue(), ENCODE));
		}
		
		return sb.toString();
	}
}
