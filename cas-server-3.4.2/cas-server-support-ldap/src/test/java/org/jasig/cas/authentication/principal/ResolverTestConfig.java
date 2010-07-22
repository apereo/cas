/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;


/**
 * Container for test properties used by LDAP resolver.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ResolverTestConfig {

    private String existsCredential;

    private String existsPrincipal;
    
    private String notExistsCredential;
    
    private String tooManyCredential;

    
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
     * @param existsPrincipal The existsPrincipal to set.
     */
    public void setExistsPrincipal(String existsPrincipal) {
        this.existsPrincipal = existsPrincipal;
    }


    /**
     * @return Returns the existsPrincipal.
     */
    public String getExistsPrincipal() {
        return this.existsPrincipal;
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

    
    /**
     * @return Returns the tooManyCredential.
     */
    public String getTooManyCredential() {
        return this.tooManyCredential;
    }

    
    /**
     * @param tooManyCredential The tooManyCredential to set.
     */
    public void setTooManyCredential(String tooManyCredential) {
        this.tooManyCredential = tooManyCredential;
    }
}