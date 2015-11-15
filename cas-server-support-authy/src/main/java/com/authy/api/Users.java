package com.authy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * 
 * @author Julian Camargo
 *
 */
public class Users extends Resource {
	public static final String NEW_USER_PATH = "/protected/xml/users/new";
	public static final String DELETE_USER_PATH = "/protected/xml/users/delete/";
	public static final String SMS_PATH = "/protected/xml/sms/";
	public static final String DEFAULT_COUNTRY_CODE = "1";
	
	public Users(String uri, String key) {
		super(uri, key);
	}
	
	public Users(String uri, String key, boolean testFlag) {
		super(uri, key, testFlag);
	}
	
	/**
	 * Create a new user using his e-mail, phone and country code.
	 * @param email email
	 * @param phone phone
	 * @param countryCode country code
	 * @return a User instance
	 */
	public com.authy.api.User createUser(String email, String phone, String countryCode) {
		Users.User user = new Users.User(email, phone, countryCode);
		
		String content = this.post(NEW_USER_PATH, user);

		return userFromXml(this.getStatus(), content);
	}
	
	/**
	 * Create a new user using his e-mail and phone. It uses USA country code by default.
	 * @param email email
	 * @param phone phone
	 * @return a User instance
	 */
	public com.authy.api.User createUser(String email, String phone) {
		return createUser(email, phone, DEFAULT_COUNTRY_CODE);
	}
	
	/**
	 * Send token via sms to a user.
	 * @param userId user id
	 * @return Hash instance with API's response.
	 */
	public Hash requestSms(int userId) {
		return requestSms(userId, Collections.EMPTY_MAP);
	}
	
	/**
	 * Send token via sms to a user with some options defined.
	 * @param userId user id
	 * @param options options
	 * @return Hash instance with API's response.
	 */
	public Hash requestSms(int userId, Map<String, String> options) {
		String url = "";
		
		try {
			url = URLEncoder.encode(Integer.toString(userId), ENCODE);
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		MapToResponse opt = new MapToResponse(options);
		String content = this.get(SMS_PATH + url, opt);
		
		return instanceFromXml(this.getStatus(), content);
	}

	/**
	 * Delete a user.
	 * @param userId user id
	 * @return Hash instance with API's response.
	 */
	public Hash deleteUser(int userId) {
		String url = "";
		
		try {
			url = URLEncoder.encode(Integer.toString(userId), ENCODE);
		}
		catch(Exception e) {
            logger.error(e.getMessage(), e);
		}
		
		String content = this.post(DELETE_USER_PATH + url, null);

		return instanceFromXml(this.getStatus(), content);
	}

	private com.authy.api.User userFromXml(int status, String content) {
		com.authy.api.User user = new com.authy.api.User();
		
		try {
			Error error = errorFromXml(status, content);
			
			if(error == null) {
				JAXBContext context = JAXBContext.newInstance(Hash.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				
				StringReader xml = new StringReader(content);
				Hash hash = (Hash)unmarshaller.unmarshal(new StreamSource(xml));
                hash.setStatus(status);
				user = hash.getUser();

			}
			user.setError(error);
            user.setStatus(status);
		}
		catch(JAXBException e) {
			logger.error(e.getMessage(), e);
		}
		return user;
	}
	
	private Error errorFromXml(int status, String content) {
		Error error = new Error();
		
		try {
			JAXBContext context = JAXBContext.newInstance(Error.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			StringReader xml = new StringReader(content);
			error = (Error)unmarshaller.unmarshal(new StreamSource(xml));
		}
		catch(JAXBException e) {
            logger.error(e.getMessage(), e);
			return null;
		}
		return error;
	}
	
	private Hash instanceFromXml(int status, String content) {
		Hash hash = new Hash();
		try {
			Error error = errorFromXml(status, content);
			if(error == null) {
				JAXBContext context = JAXBContext.newInstance(Hash.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
			
				StringReader xml = new StringReader(content);
				hash = (Hash)unmarshaller.unmarshal(new StreamSource(xml));
			}
			hash.setError(error);
            hash.setStatus(status);
		}
		catch(JAXBException e) {
			logger.error(e.getMessage(), e);
		}
		return hash;
	}

	static class MapToResponse implements Response {
		private Map<String, String> options;
		
		public MapToResponse(Map<String, String> options) {
			this.options = options;
		}
		
		public String toXML() {
			return "";
		}

		public Map<String, String> toMap() {
			return options;
		}
	}
	
	@XmlRootElement(name="user")
	static class User implements Response {
        protected final Logger logger = LoggerFactory.getLogger(getClass());

		String email, cellphone, countryCode;

		public User() {
		}
		
		public User(String email, String cellphone, String countryCode) {
			this.email = email;
			this.cellphone = cellphone;
			this.countryCode = countryCode;
		}
		
		@XmlElement(name="email")
		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@XmlElement(name="cellphone")
		public String getCellphone() {
			return cellphone;
		}

		public void setCellphone(String cellphone) {
			this.cellphone = cellphone;
		}

		@XmlElement(name="country_code")
		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}
		
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

		public Map<String, String> toMap() {
			return Collections.EMPTY_MAP;
		}
	}
}
