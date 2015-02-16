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
package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We utilize the {@link org.jasig.cas.authentication.AuthenticationMetaDataPopulator} to retrieve and store
 * the password as an authentication attribute under the key
 * {@link UsernamePasswordCredential#AUTHENTICATION_ATTRIBUTE_PASSWORD}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        logger.debug("Processing request to capture the credential for [{}]", credential.getId());
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        builder.addAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, c.getPassword());
        logger.debug("Encrypted credential is added as the authentication attribute [{}] to the authentication",
                    UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);

    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
