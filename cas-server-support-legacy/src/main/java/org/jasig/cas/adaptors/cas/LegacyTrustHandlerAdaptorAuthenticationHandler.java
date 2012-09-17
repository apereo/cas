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
package org.jasig.cas.adaptors.cas;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.util.StringUtils;

import edu.yale.its.tp.cas.auth.TrustHandler;

import javax.validation.constraints.NotNull;

/**
 * Adaptor class to adapt the legacy CAS TrustHandler to the new
 * AuthenticationHandler
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LegacyTrustHandlerAdaptorAuthenticationHandler implements
    AuthenticationHandler {

    @NotNull
    private TrustHandler trustHandler;

    public boolean authenticate(final Credentials credentials) {
        final LegacyCasTrustedCredentials casCredentials = (LegacyCasTrustedCredentials) credentials;

        return StringUtils.hasText(this.trustHandler.getUsername(casCredentials
            .getServletRequest()));
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && LegacyCasTrustedCredentials.class.equals(credentials.getClass());
    }

    /**
     * @param trustHandler The trustHandler to set.
     */
    public void setTrustHandler(final TrustHandler trustHandler) {
        this.trustHandler = trustHandler;
    }
}