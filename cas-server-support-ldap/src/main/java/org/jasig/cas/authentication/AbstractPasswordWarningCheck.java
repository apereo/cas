/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class providing common functionality for checking the account's
 * expiration time.
 * 
 * @author Eric Pierce
 * @author Jan Van der Velpen
 * @version 1.1 3/30/2009 11:47:37
 * 
 */
public abstract class AbstractPasswordWarningCheck implements PasswordWarningCheck, InitializingBean {

    /** Instance of logging for subclasses. */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int STATUS_PASS = -1;
    public static final int STATUS_ERROR = -2;
    
}
