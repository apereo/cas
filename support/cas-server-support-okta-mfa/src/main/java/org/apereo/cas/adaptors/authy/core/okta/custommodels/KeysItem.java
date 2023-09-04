package org.apereo.cas.adaptors.authy.core.okta.custommodels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeysItem{

	@JsonProperty("kty")
	private String kty;

	@JsonProperty("e")
	private String e;

	@JsonProperty("use")
	private String use;

	@JsonProperty("kid")
	private String kid;

	@JsonProperty("n")
	private String n;

	public void setKty(String kty){
		this.kty = kty;
	}

	public String getKty(){
		return kty;
	}

	public void setE(String e){
		this.e = e;
	}

	public String getE(){
		return e;
	}

	public void setUse(String use){
		this.use = use;
	}

	public String getUse(){
		return use;
	}

	public void setKid(String kid){
		this.kid = kid;
	}

	public String getKid(){
		return kid;
	}

	public void setN(String n){
		this.n = n;
	}

	public String getN(){
		return n;
	}

	@Override
 	public String toString(){
		return 
			"KeysItem{" + 
			"kty = '" + kty + '\'' + 
			",e = '" + e + '\'' + 
			",use = '" + use + '\'' + 
			",kid = '" + kid + '\'' + 
			",n = '" + n + '\'' + 
			"}";
		}
}