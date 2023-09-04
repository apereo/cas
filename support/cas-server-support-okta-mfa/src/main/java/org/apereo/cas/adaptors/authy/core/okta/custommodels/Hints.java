package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hints{

	@JsonProperty("allow")
	private List<String> allow;

	public void setAllow(List<String> allow){
		this.allow = allow;
	}

	public List<String> getAllow(){
		return allow;
	}

	@Override
 	public String toString(){
		return 
			"Hints{" + 
			"allow = '" + allow + '\'' + 
			"}";
		}
}