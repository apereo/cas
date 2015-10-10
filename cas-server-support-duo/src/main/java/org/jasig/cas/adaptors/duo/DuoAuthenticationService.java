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

import com.duosecurity.duoweb.DuoWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 * Derived from the fine work of @author Eric Pierce <epierce@usf.edu>
 * and @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class DuoAuthenticationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String duoIntegrationKey;
    private final String duoSecretKey;
    private final String duoApplicationKey;
    private final String duoApiHost;

    /**
     * Creates the duo authentication service.
     * @param duoIntegrationKey duo integration key
     * @param duoSecretKey duo secret key
     * @param duoApplicationKey duo application key
     * @param duoApiHost duo API host url
     */
    public DuoAuthenticationService(final String duoIntegrationKey, final String duoSecretKey,
                             final String duoApplicationKey, final String duoApiHost) {
        this.duoIntegrationKey = duoIntegrationKey;
        this.duoSecretKey = duoSecretKey;
        this.duoApplicationKey = duoApplicationKey;
        this.duoApiHost = duoApiHost;
    }

    public String getDuoApiHost() {
        return this.duoApiHost;
    }

    public String getDuoIntegrationKey() {
        return duoIntegrationKey;
    }

    public String getDuoSecretKey() {
        return duoSecretKey;
    }

    public String getDuoApplicationKey() {
        return duoApplicationKey;
    }

    /**
     * Sign the authentication request.
     * @param username username requesting authentication
     * @return signed response
     */
    public String generateSignedRequestToken(final String username) {
        return DuoWeb.signRequest(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, username);
    }

    /**
     * Verify the authentication response from Duo.
     * @param signedRequestToken signed request token
     * @return authenticated user
     * @throws Exception if response verification fails
     */
    public String authenticate(final String signedRequestToken) throws Exception {
        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, signedRequestToken);
    }
}
