/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.util.UrlUtils;

/**
 * Class to validate the credentials presented by communicating with the web
 * server and checking the certificate that is returned against the hostname,
 * etc.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandler extends
    AbstractAuthenticationHandler {

    /** The string represetning the HTTPS protocol. */
    private static final String PROTOCOL_HTTPS = "https";

    /** Do we allow null responses. Usually indicates a redirect or not */
    private boolean allowNullResponses = false;

    public boolean authenticateInternal(final Credentials credentials) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        String response = null;
        if (!serviceCredentials.getCallbackUrl().getProtocol().equals(
            PROTOCOL_HTTPS)) {
            return false;
        }
        getLog()
            .debug("Attempting to resolve credentials for "
                + serviceCredentials);
        try {
            response = UrlUtils.getResponseBodyFromUrl(serviceCredentials
                .getCallbackUrl());
            getLog().debug(response);
        } catch (Exception e) {
            getLog().error(e);
            return false;
        }

        return this.allowNullResponses ? true : response != null;
    }

    protected boolean supports(final Credentials credentials) {
        return HttpBasedServiceCredentials.class.isAssignableFrom(credentials
            .getClass());
    }

    public void setAllowNullResponses(final boolean allowNullResponses) {
        this.allowNullResponses = allowNullResponses;
    }
}
