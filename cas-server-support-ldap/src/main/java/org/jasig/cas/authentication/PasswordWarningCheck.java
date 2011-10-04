/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * Interface for a class that fetches an account status.
 * 
 * @author Jan Van der Velpen
 * @version $Revision: 1.0 $ $Date: 2006/12/13 14:28:05 $
 * @since 3.1
 */
public interface PasswordWarningCheck {
    /**
     * @param userID The unique ID of the user
     * @return Code for this status
     */
    public int getPasswordWarning(String userID);
    
}
