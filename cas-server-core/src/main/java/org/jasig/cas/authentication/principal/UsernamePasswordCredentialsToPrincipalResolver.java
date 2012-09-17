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

/**
 * Implementation of CredentialsToPrincipalResolver for Credentials based on
 * UsernamePasswordCredentials when a SimplePrincipal (username only) is
 * sufficient.
 * <p>
 * Implementation extracts the username from the Credentials provided and
 * constructs a new SimplePrincipal with the unique id set to the username.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.2 $ $Date: 2007/01/22 20:35:26 $
 * @since 3.0
 * @see org.jasig.cas.authentication.principal.SimplePrincipal
 */
public final class UsernamePasswordCredentialsToPrincipalResolver extends
    AbstractPersonDirectoryCredentialsToPrincipalResolver {

    protected String extractPrincipalId(final Credentials credentials) {
        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
        return usernamePasswordCredentials.getUsername();
    }

    /**
     * Return true if Credentials are UsernamePasswordCredentials, false
     * otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
