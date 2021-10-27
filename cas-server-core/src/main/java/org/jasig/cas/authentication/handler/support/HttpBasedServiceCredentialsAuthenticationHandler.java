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

import java.security.GeneralSecurityException;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

/**
 * Class to validate the credential presented by communicating with the web
 * server and checking the certificate that is returned against the hostname,
 * etc.
 * <p>
 * This class is concerned with ensuring that the protocol is HTTPS and that a
 * response is returned. The SSL handshake that occurs automatically by opening
 * a connection does the heavy process of authenticating.
 *
 * @author Scott Battaglia

 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    /** The string representing the HTTPS protocol. */
    private static final String PROTOCOL_HTTPS = "https";

    /** Boolean variable denoting whether secure connection is required or not. */
    private boolean requireSecure = true;

    /** Log instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Instance of Apache Commons HttpClient. */
    @NotNull
    private HttpClient httpClient;

    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final HttpBasedServiceCredential httpCredential = (HttpBasedServiceCredential) credential;
        if (this.requireSecure && !httpCredential.getCallbackUrl().getProtocol().equals(PROTOCOL_HTTPS)) {
            logger.debug("Authentication failed because url was not secure.");
            throw new FailedLoginException(httpCredential.getCallbackUrl() + " is not an HTTPS endpoint as required.");
        }
        logger.debug("Attempting to authenticate {}", httpCredential);
        if (!this.httpClient.isValidEndPoint(httpCredential.getCallbackUrl())) {
            throw new FailedLoginException(
                    httpCredential.getCallbackUrl() + " sent an unacceptable response status code");
        }
        return new HandlerResult(this, httpCredential, new SimplePrincipal(httpCredential.getId()));
    }

    /**
     * @return true if the credential provided are not null and the credential
     * are a subclass of (or equal to) HttpBasedServiceCredential.
     */
    public boolean supports(final Credential credential) {
        return credential instanceof HttpBasedServiceCredential;
    }

    /**
     * Sets the HttpClient which will do all of the connection stuff.
     * @param httpClient http client instance to use
     **/
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
