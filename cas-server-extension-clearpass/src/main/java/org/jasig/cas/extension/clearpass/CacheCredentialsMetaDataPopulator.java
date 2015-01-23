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
package org.jasig.cas.extension.clearpass;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * We cheat and utilize the {@link org.jasig.cas.authentication.AuthenticationMetaDataPopulator} to retrieve and store
 * the password in our cache rather than use the original design which relied on modifying the flow.  This method, while
 * technically a misuse of the interface relieves us of having to modify and maintain the login flow manually.
 *
 * @deprecated As of 4.1, use {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator} instead.
 * @author Scott Battaglia
 * @since 1.0
 */
@Deprecated
public final class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @NotNull
    private final Map<String, String> credentialCache;

    /**
     * Instantiates a new cache credentials meta data populator.
     *
     * @param credentialCache the credential cache
     */
    public CacheCredentialsMetaDataPopulator(final Map<String, String> credentialCache) {
        this.credentialCache = credentialCache;
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        final Authentication authentication = builder.build();
        this.credentialCache.put(authentication.getPrincipal().getId(), c.getPassword());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
