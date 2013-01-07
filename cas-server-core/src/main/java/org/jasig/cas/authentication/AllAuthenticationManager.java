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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Authentication manager that employs a simple "allow all" strategy to authenticate a set of credentials.
 * The {@link AuthenticationManager#authenticate(Credential...)} succeeds if and only if all given
 * credentials are authenticated.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AllAuthenticationManager extends AbstractAuthenticationManager {

    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers List of authentication handlers.
     */
    public AllAuthenticationManager(final List<AuthenticationHandler> handlers ) {
        super(handlers);
    }

    /**
     * Creates a new authentication manager with a map of authentication handlers to the principal resolvers that
     * should be used upon successful authentication if no principal is resolved by the authentication handler. If
     * the order of evaluation of authentication handlers is important, a map that preserves insertion order
     * (e.g. {@link java.util.LinkedHashMap}) should be used.
     *
     * @param map A map of authentication handler to principal resolver.
     */
    public AllAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        super(map);
    }

    @Override
    protected boolean isSatisfied(
            final Collection<Credential> credentials,
            final Map<Credential, HandlerResult> successes,
            final Map<Credential, HandlerError> failures,
            final Authentication authentication) {
        return credentials.size() == successes.size();
    }
}
