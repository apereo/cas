package org.jasig.cas.adaptors.ldap.util;

public class LDAPErrorDef {
	
	public String errMessage;
	
	public String ldapPattern;
	
	public void setErrMessage(String errMessage){
		this.errMessage=errMessage;
	}
	
	public void setLDAPPattern(String ldapPattern){
		this.ldapPattern=ldapPattern;
	}

}
