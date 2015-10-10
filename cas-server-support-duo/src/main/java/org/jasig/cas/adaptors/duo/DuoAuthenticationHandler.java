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

package org.jasig.cas.adaptors.duo


import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import java.security.GeneralSecurityException;

public final class DuoAuthenticationHandler implements AbstractPreAndPostProcessingAuthenticationHandler {

    private final DuoAuthenticationService duoAuthenticationService

    public DuoAuthenticationHandler(final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService
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
        final DuoCredential duoCredential = (DuoCredential) credential;

        final duoVerifyResponse = this.duoAuthenticationService.authenticate(duoCredential.signedDuoResponse);
        logger.debug("Response from Duo verify: [{}]", duoVerifyResponse);
        final String primaryCredentialsUsername = duoCredential.username;

        final isGoodAuthentication = duoVerifyResponse == primaryCredentialsUsername
        if (isGoodAuthentication) {
            logger.info("Successful Duo authentication for [{}]", primaryCredentialsUsername)
            return true
        }
        logger.error("Duo authentication error! Login username: [{}], Duo response: [{}]",
                primaryCredentialsUsername ? : "null", duoVerifyResponse);

    }

    @Override
    public boolean supports(final Credential credential) {
        return DuoCredential.isAssignableFrom(credential.getClass());
    }
}
