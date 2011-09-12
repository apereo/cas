/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Credentials that wish to handle remember me scenarios need
 * to implement this class.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public interface RememberMeCredentials extends Credentials {
    
    String AUTHENTICATION_ATTRIBUTE_REMEMBER_ME = "org.jasig.cas.authentication.principal.REMEMBER_ME";
    
    String REQUEST_PARAMETER_REMEMBER_ME = "rememberMe";

    boolean isRememberMe();
    
    void setRememberMe(boolean rememberMe);
}
