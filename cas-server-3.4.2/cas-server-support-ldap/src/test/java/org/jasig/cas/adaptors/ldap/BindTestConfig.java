/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;


/**
 * Contains test data to facilitate customizing LDAP bind authentication
 * tests for arbitrary LDAP environment.
 *
 * @author marvin
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class BindTestConfig {
    /** Name of credential that exists in target LDAP */
    private String existsCredential;
   
    /** Correct password for test credential */
    private String existsSuccessPassword;
    
    /** Incorrect password for test credential */
    private String existsFailurePassword;
  
    /** A credential that does not exist in the target LDAP */
    private String notExistsCredential;

    
    /**
     * @return Returns the existsCredential.
     */
    public String getExistsCredential() {
        return this.existsCredential;
    }

    
    /**
     * @param existsCredential The existsCredential to set.
     */
    public void setExistsCredential(String existsCredential) {
        this.existsCredential = existsCredential;
    }

    
    /**
     * @return Returns the existsSuccessPassword.
     */
    public String getExistsSuccessPassword() {
        return this.existsSuccessPassword;
    }

    
    /**
     * @param existsSuccessPassword The existsSuccessPassword to set.
     */
    public void setExistsSuccessPassword(String existsSuccessPassword) {
        this.existsSuccessPassword = existsSuccessPassword;
    }

    
    /**
     * @return Returns the existsFailurePassword.
     */
    public String getExistsFailurePassword() {
        return this.existsFailurePassword;
    }

    
    /**
     * @param existsFailurePassword The existsFailurePassword to set.
     */
    public void setExistsFailurePassword(String existsFailurePassword) {
        this.existsFailurePassword = existsFailurePassword;
    }

    
    /**
     * @return Returns the notExistsCredential.
     */
    public String getNotExistsCredential() {
        return this.notExistsCredential;
    }

    
    /**
     * @param notExistsCredential The notExistsCredential to set.
     */
    public void setNotExistsCredential(String notExistsCredential) {
        this.notExistsCredential = notExistsCredential;
    }



}
