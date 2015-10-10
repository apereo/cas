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

package org.jasig.cas.adaptors.duo;


import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Authenticate CAS credentials against Duo Security.
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Component("duoAuthenticationHandler")
public final class DuoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final DuoAuthenticationService duoAuthenticationService;

    /**
     * Creates the duo authentication handler.
     * @param duoAuthenticationService the duo authentication service
     */
    @Autowired

    public DuoAuthenticationHandler(@Qualifier("duoAuthenticationService")
                                        final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }


    /**
     * Do an out of band request using the DuoWeb api (encapsulated in DuoAuthenticationService) to the hosted duo service,
     * if it is successful
     * it will return a String containing the username of the successfully authenticated user, but if not - will
     * return a blank String or null.
     * @param credential Credential to authenticate.
     *
     * @throws GeneralSecurityException
     * @throws PreventedException
     */
    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        try {
            final DuoCredential duoCredential = (DuoCredential) credential;

            final String duoVerifyResponse = this.duoAuthenticationService.authenticate(duoCredential.getSignedDuoResponse());
            logger.debug("Response from Duo verify: [{}]", duoVerifyResponse);
            final String primaryCredentialsUsername = duoCredential.getUsername();

            final boolean isGoodAuthentication= duoVerifyResponse.equals(primaryCredentialsUsername);

            if (isGoodAuthentication) {
                logger.info("Successful Duo authentication for [{}]", primaryCredentialsUsername);

                final Principal principal = this.principalFactory.createPrincipal(primaryCredentialsUsername);
                return createHandlerResult(credential, principal, new ArrayList<MessageDescriptor>());
            }
            throw new FailedLoginException("Duo authentication error for username "
                    + primaryCredentialsUsername + " and Duo response: " + duoVerifyResponse);

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new FailedLoginException(e.getMessage());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return DuoCredential.class.isAssignableFrom(credential.getClass());
    }
}
