package com.authy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

/**
 * 
 * @author Julian Camargo
 *
 */
@XmlRootElement(name="hash")
public class Hash extends Instance implements Response {

    private final Logger logger = LoggerFactory.getLogger(getClass());

	private User user = null;
	private String message, token;
	private boolean success;
	
	public Hash() {
	}
	
	public Hash(int status, String content) {
		super(status, content);
	}
	
	@XmlElement(type=User.class)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

    @Override
    public boolean isOk() {
        return isSuccess();
    }

    /**
	 * Map a Token instance to its XML representation.
	 * @return a String with the description of this object in XML.
	 */
	public String toXML() {
		Error error = getError();
		
		if(error != null) {
			return error.toXML();
		}
		
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
		return Collections.EMPTY_MAP;
	}
}
