/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Class to validate the credentials presented by communicating with the web
 * server and checking the certificate that is returned against the hostname,
 * etc.
 * <p>
 * This class is concerned with ensuring that the protocol is HTTPS and that a
 * response is returned. The SSL handshake that occurs automatically by opening
 * a connection does the heavy process of authenticating.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandler implements AuthenticationHandler {

    /** The string representing the HTTPS protocol. */
    private static final String PROTOCOL_HTTPS = "https";

    /** Boolean variable denoting whether secure connection is required or not. */
    private boolean requireSecure = true;

    /** Log instance. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Instance of Apache Commons HttpClient */
    @NotNull
    private HttpClient httpClient;

    public boolean authenticate(final Credentials credentials) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        if (this.requireSecure
            && !serviceCredentials.getCallbackUrl().getProtocol().equals(
                PROTOCOL_HTTPS)) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication failed because url was not secure.");
            }
            return false;
        }
        log
            .debug("Attempting to resolve credentials for "
                + serviceCredentials);

        return this.httpClient.isValidEndPoint(serviceCredentials
            .getCallbackUrl());
    }

    /**
     * @return true if the credentials provided are not null and the credentials
     * are a subclass of (or equal to) HttpBasedServiceCredentials.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && HttpBasedServiceCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    /** Sets the HttpClient which will do all of the connection stuff. */
    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Set whether a secure url is required or not.
     * 
     * @param requireSecure true if its required, false if not. Default is true.
     */
    public void setRequireSecure(final boolean requireSecure) {
        this.requireSecure = requireSecure;
    }
}
