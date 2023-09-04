package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Profile{

	@JsonProperty("deviceType")
	private String deviceType;

	@JsonProperty("keys")
	private List<KeysItem> keys;

	@JsonProperty("name")
	private String name;

	@JsonProperty("credentialId")
	private String credentialId;

	@JsonProperty("version")
	private String version;

	@JsonProperty("platform")
	private String platform;

	public void setDeviceType(String deviceType){
		this.deviceType = deviceType;
	}

	public String getDeviceType(){
		return deviceType;
	}

	public void setKeys(List<KeysItem> keys){
		this.keys = keys;
	}

	public List<KeysItem> getKeys(){
		return keys;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setCredentialId(String credentialId){
		this.credentialId = credentialId;
	}

	public String getCredentialId(){
		return credentialId;
	}

	public void setVersion(String version){
		this.version = version;
	}

	public String getVersion(){
		return version;
	}

	public void setPlatform(String platform){
		this.platform = platform;
	}

	public String getPlatform(){
		return platform;
	}

	@Override
 	public String toString(){
		return 
			"Profile{" + 
			"deviceType = '" + deviceType + '\'' + 
			",keys = '" + keys + '\'' + 
			",name = '" + name + '\'' + 
			",credentialId = '" + credentialId + '\'' + 
			",version = '" + version + '\'' + 
			",platform = '" + platform + '\'' + 
			"}";
		}
}