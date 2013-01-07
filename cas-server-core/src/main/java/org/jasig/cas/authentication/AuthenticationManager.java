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
package org.jasig.cas.authentication;

/**
 * Authentication managers employ various strategies to authenticate one or more credentials.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 * @see AuthenticationHandler
 */
public interface AuthenticationManager {
    
    String AUTHENTICATION_METHOD_ATTRIBUTE = "authenticationMethod";

    /**
     * Authenticates the given credentials. Depending on the strategy employed by the component, one, several, or all
     * credentials may be authenticated. At least one credential MUST be authenticated in order for authentication to
     * succeed. The credential must be resolved into a principal by some means as part of authentication. Implementers
     * SHOULD treat failure to resolve a principal as an authentication failure.
     *
     *
     * @param credentials One or more credentials to authenticate.
     *
     * @return An authentication containing a resolved principal on success.
     *
     * @throws AuthenticationException When one or more credentials failed to validate such that security policy
     * was not satisfied.
     * @throws PrincipalException When there is a problem resolving a principal on successful authentication.
     */
    Authentication authenticate(final Credential ... credentials) throws AuthenticationException, PrincipalException;
}
