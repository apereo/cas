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
package org.jasig.cas.support.oauth.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class resolves the principal id regarding the OAuth credentials : principal id is the type of the provider # the user identifier.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthCredentialsToPrincipalResolver.class);
    
    @Override
    protected String extractPrincipalId(final Credentials credentials) {
        OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        String principalId = oauthCredentials.getProviderType() + "#" + oauthCredentials.getUserId();
        logger.debug("principalId : {}", principalId);
        return principalId;
    }
    
    /**
     * Return true if Credentials are OAuthCredentials, false otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }
}
