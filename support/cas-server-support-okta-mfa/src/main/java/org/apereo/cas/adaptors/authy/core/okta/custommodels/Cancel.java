package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cancel{

	@JsonProperty("hints")
	private Hints hints;

	@JsonProperty("href")
	private String href;

	public void setHints(Hints hints){
		this.hints = hints;
	}

	public Hints getHints(){
		return hints;
	}

	public void setHref(String href){
		this.href = href;
	}

	public String getHref(){
		return href;
	}

	@Override
 	public String toString(){
		return 
			"Cancel{" + 
			"hints = '" + hints + '\'' + 
			",href = '" + href + '\'' + 
			"}";
		}
}