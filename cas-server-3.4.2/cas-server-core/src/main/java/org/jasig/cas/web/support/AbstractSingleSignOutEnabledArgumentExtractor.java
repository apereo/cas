/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for handling the enabling and disabling of Single Sign Out features.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public abstract class AbstractSingleSignOutEnabledArgumentExtractor implements
    ArgumentExtractor {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Whether single sign out is disabled or not. */
    private boolean disableSingleSignOut = false;
    
    /** Default instance of HttpClient. */
    @NotNull
    private HttpClient httpClient;
    
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    protected HttpClient getHttpClientIfSingleSignOutEnabled() {
        return !this.disableSingleSignOut ? this.httpClient : null; 
    }
    
    public void setDisableSingleSignOut(final boolean disableSingleSignOut) {
        this.disableSingleSignOut = disableSingleSignOut;
    }
    
    public final WebApplicationService extractService(final HttpServletRequest request) {
        final WebApplicationService service = extractServiceInternal(request);
        
        if (service == null) {
            log.debug("Extractor did not generate service.");
        } else {
            log.debug("Extractor generated service for: " + service.getId());
        }
        
        return service;
    }
    
    protected abstract WebApplicationService extractServiceInternal(final HttpServletRequest request);
}
