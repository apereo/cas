/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import java.net.URL;
import java.security.GeneralSecurityException;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

 * @since 3.0.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    /** Log instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Instance of Apache Commons HttpClient. */
    @NotNull
    private HttpClient httpClient;

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final HttpBasedServiceCredential httpCredential = (HttpBasedServiceCredential) credential;
        if (!httpCredential.getService().getProxyPolicy().isAllowedProxyCallbackUrl(httpCredential.getCallbackUrl())) {
            logger.warn("Proxy policy for service [{}] cannot authorize the requested callbackurl [{}]",
                    httpCredential.getService(), httpCredential.getCallbackUrl());
            throw new FailedLoginException(httpCredential.getCallbackUrl() + " cannot be authorized");
        }

        logger.debug("Attempting to authenticate {}", httpCredential);
        final URL callbackUrl = httpCredential.getCallbackUrl();
        if (!this.httpClient.isValidEndPoint(callbackUrl)) {
            throw new FailedLoginException(callbackUrl.toExternalForm() + " sent an unacceptable response status code");
        }
        return new DefaultHandlerResult(this, httpCredential, this.principalFactory.createPrincipal(httpCredential.getId()));
    }

    /**
     * {@inheritDoc}
     * @return true if the credential provided are not null and the credential
     * are a subclass of (or equal to) HttpBasedServiceCredential.
     */
    @Override
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
     * @deprecated As of 4.1. Endpoint security is handled by service proxy policies
     *
     * <p>Set whether a secure url is required or not.</p>
     *
     * @param requireSecure true if its required, false if not. Default is true.
     */
    @Deprecated
    public void setRequireSecure(final boolean requireSecure) {
         logger.warn("setRequireSecure() is deprecated and will be removed. Callback url validation is controlled by the proxy policy");
    }
}
