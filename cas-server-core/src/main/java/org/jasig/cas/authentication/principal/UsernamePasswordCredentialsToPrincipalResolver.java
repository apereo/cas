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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.PasswordCredential;

/**
 * Implementation of CredentialsToPrincipalResolver for Credential based on
 * PasswordCredential when a SimplePrincipal (username only) is
 * sufficient.
 * <p>
 * Implementation extracts the username from the Credential provided and
 * constructs a new SimplePrincipal with the unique id set to the username.
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public final class UsernamePasswordCredentialsToPrincipalResolver extends
    AbstractPersonDirectoryCredentialsToPrincipalResolver {

    protected String extractPrincipalId(final Credential credential) {
        final PasswordCredential passwordCredentials = (PasswordCredential) credential;
        return passwordCredentials.getUsername();
    }

    /**
     * Return true if Credential are PasswordCredential, false
     * otherwise.
     * @return true, if the credential are supported by this resolver
     */
    public boolean supports(final Credential credential) {
        return credential != null
            && PasswordCredential.class.isAssignableFrom(credential
                .getClass());
    }
}
