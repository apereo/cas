/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.SamlService;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.util.HttpClient;

/**
 * Retrieve the ticket and artifact based on the SAML 1.1 profile.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SamlArgumentExtractor implements ArgumentExtractor {
    
    private HttpClient httpClient = new HttpClient();

    public WebApplicationService extractService(final HttpServletRequest request) {
        return SamlService.createServiceFrom(request, this.httpClient);
    }
    
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
