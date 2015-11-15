package com.authy.api;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Julian Camargo
 *
 */
@XmlRootElement(name="user")
public class User extends Instance implements Response {
	int id;
	
	public User() {
	}

	public User(int status, String content) {
		super(status, content);
	}

	@XmlElement(name="id")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("id", Integer.toString(id));
		map.put("status", Integer.toString(status));
		map.put("content", content);
		
		return map;
	}
}
