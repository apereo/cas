/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.web.support.ArgumentExtractor;

/**
 * Constructs an OpenId Service.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdArgumentExtractor implements ArgumentExtractor {

    public WebApplicationService extractService(final HttpServletRequest request) {
        return OpenIdService.createServiceFrom(request);
    }
}
