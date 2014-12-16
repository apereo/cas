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
package org.jasig.cas.support.saml.web.support;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Constructs a GoogleAccounts compatible service and provides the public and
 * private keys.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class GoogleAccountsArgumentExtractor extends AbstractArgumentExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountsArgumentExtractor.class);

    @NotNull
    private final PublicKey publicKey;

    @NotNull
    private final PrivateKey privateKey;

    @NotNull
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new google accounts argument extractor.
     *
     * @param publicKey the public key
     * @param privateKey the private key
     * @param servicesManager the services manager
     */
    public GoogleAccountsArgumentExtractor(final PublicKey publicKey,
                                           final PrivateKey privateKey, final ServicesManager servicesManager) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.servicesManager = servicesManager;
    }

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return GoogleAccountsService.createServiceFrom(request,
                this.privateKey, this.publicKey, this.servicesManager);
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param privateKey the private key object
     */
    @Deprecated
    public void setPrivateKey(final PrivateKey privateKey) {
        LOGGER.warn("setPrivateKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. Use Ctors instead.
     * @param publicKey the public key object
     */
    @Deprecated
    public void setPublicKey(final PublicKey publicKey) {
        LOGGER.warn("setPublicKey() is deprecated and has no effect. Consider using constructors instead.");
    }

    /**
     * @deprecated As of 4.1. The behavior is controlled by the service registry instead.
     * Sets an alternate username to send to Google (i.e. fully qualified email address).  Relies on an appropriate
     * attribute available for the user.
     * <p>
     * Note that this is optional and the default is to use the normal identifier.
     *
     * @param alternateUsername the alternate username. This is OPTIONAL.
     */
    @Deprecated
    public void setAlternateUsername(final String alternateUsername) {
        LOGGER.warn("setAlternateUsername() is deprecated and has no effect. Instead use the configuration in service registry.");
    }
}
