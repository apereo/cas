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
package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationHandler which authenticates Principal-bearing credentials.
 * Authentication must have occurred at the time the Principal-bearing
 * credentials were created, so we perform no further authentication. Thus
 * merely by being presented a PrincipalBearingCredentials, this handler returns
 * true.
 *
 * @author Andrew Petro
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandler implements AuthenticationHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean authenticate(final Credentials credentials) {
        logger.debug("Trusting credentials for: {}", credentials);
        return true;
    }

    @Override
    public boolean supports(final Credentials credentials) {
        return credentials.getClass().equals(PrincipalBearingCredentials.class);
    }
}
