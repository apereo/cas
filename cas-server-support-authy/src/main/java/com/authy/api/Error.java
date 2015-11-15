package com.authy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Julian Camargo
 *
 */
@XmlRootElement(name="errors")
public class Error implements Response {
    private final Logger logger = LoggerFactory.getLogger(getClass());

	private String message, url, countryCode;

	@XmlElement(name="country-code")
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@XmlElement(name="message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlElement(name="url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Map a Token instance to its XML representation.
	 * @return a String with the description of this object in XML.
	 */
	public String toXML() {
		StringWriter sw = new StringWriter();
		String xml = "";
		
		try {
			JAXBContext context = JAXBContext.newInstance(this.getClass());
			Marshaller marshaller = context.createMarshaller();
			
			marshaller.marshal(this, sw);
			xml = sw.toString();
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return xml;
	}

	/**
	 * Map a Token instance to its Java's Map representation.
	 * @return a Java's Map with the description of this object.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("message", message);
		map.put("country-code", countryCode);
		map.put("url", url);
		
		return map;
	}

	@Override
	public String toString() {
		return "Error [message=" + message + ", url=" + url + ", countryCode="
				+ countryCode + "]";
	}

}
