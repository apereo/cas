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
