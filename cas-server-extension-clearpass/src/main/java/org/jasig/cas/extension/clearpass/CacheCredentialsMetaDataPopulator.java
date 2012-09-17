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

package org.jasig.cas.extension.clearpass;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * We cheat and utilize the {@link org.jasig.cas.authentication.AuthenticationMetaDataPopulator} to retrieve and store
 * the password in our cache rather than use the original design which relied on modifying the flow.  This method, while
 * technically a misuse of the interface relieves us of having to modify and maintain the login flow manually.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0
 */
public final class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @NotNull
    private Map<String,String> credentialCache;

    public CacheCredentialsMetaDataPopulator(final Map<String,String> credentialCache) {
        this.credentialCache = credentialCache;
    }

    public Authentication populateAttributes(final Authentication authentication, final Credentials credentials) {
        if (credentials instanceof UsernamePasswordCredentials) {
            final UsernamePasswordCredentials c = (UsernamePasswordCredentials) credentials;
            this.credentialCache.put(authentication.getPrincipal().getId(), c.getPassword());
        }

        return authentication;
    }
}
