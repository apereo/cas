package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomVerifyUserFactorResponse{

	@JsonProperty("_links")
	private Links links;

	@JsonProperty("profile")
	private Profile profile;

	@JsonProperty("factorResult")
	private String factorResult;

	@JsonProperty("expiresAt")
	private String expiresAt;

	public void setLinks(Links links){
		this.links = links;
	}

	public Links getLinks(){
		return links;
	}

	public void setProfile(Profile profile){
		this.profile = profile;
	}

	public Profile getProfile(){
		return profile;
	}

	public void setFactorResult(String factorResult){
		this.factorResult = factorResult;
	}

	public String getFactorResult(){
		return factorResult;
	}

	public void setExpiresAt(String expiresAt){
		this.expiresAt = expiresAt;
	}

	public String getExpiresAt(){
		return expiresAt;
	}

	public String getTransactionId(){
		return links.getPoll().getHref().substring(links.getPoll().getHref().lastIndexOf('/') + 1);
	}

	@Override
 	public String toString(){
		return 
			"CustomVerifyUserFactorResponse{" + 
			"_links = '" + links + '\'' + 
			",profile = '" + profile + '\'' + 
			",factorResult = '" + factorResult + '\'' + 
			",expiresAt = '" + expiresAt + '\'' + 
			"}";
		}
}