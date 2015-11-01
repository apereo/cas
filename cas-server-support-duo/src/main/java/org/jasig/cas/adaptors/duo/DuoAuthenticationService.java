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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Component("duoAuthenticationService")
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
    @Autowired
    public DuoAuthenticationService(@Value("${cas.duo.integration.key:}") final String duoIntegrationKey,
                                    @Value("${cas.duo.secret.key:}") final String duoSecretKey,
                                    @Value("${cas.duo.application.key:}") final String duoApplicationKey,
                                    @Value("${cas.duo.api.host:}") final String duoApiHost) {
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
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }
        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, signedRequestToken);
    }
}
