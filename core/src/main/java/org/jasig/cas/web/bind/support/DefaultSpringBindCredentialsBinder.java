/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web.bind.support;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.bind.CredentialsBinder;

/**
 * The default custom binder for credentials. It doesn't do anything as it
 * assumes you're using the Spring bind capabilties.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultSpringBindCredentialsBinder implements CredentialsBinder {

    public void bind(HttpServletRequest request, Credentials credentials) {
        // don't do anything, Spring binding handles everything already due to
        // the controller
    }

    public boolean supports(Class clazz) {
        return true; // supports any kind of credentials
    }
}