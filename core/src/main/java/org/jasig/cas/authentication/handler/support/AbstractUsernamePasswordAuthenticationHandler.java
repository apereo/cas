/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredentials.
 * 
 * @author Scott Battaglia
 * @version $Id: AbstractUsernamePasswordAuthenticationHandler.java,v 1.2
 * 2005/02/27 05:49:26 sbattaglia Exp $
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends
    AbstractAuthenticationHandler implements InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    protected final boolean supports(final Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}