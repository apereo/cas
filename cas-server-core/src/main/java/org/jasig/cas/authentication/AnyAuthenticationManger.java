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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication manager that employs a simple "allow any" strategy to authenticate a set of credentials.
 * The {@link #authenticate(Credential...)} method returns immediately upon the
 * first credential that is successfully authenticated by any of the configured {@link AuthenticationHandler}s.
 * Credentials are evaluated in the order given against each of the configured handlers. Only handlers for which
 * {@link AuthenticationHandler#supports(Credential)} returns true attempt to
 * actually authenticate the credential.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AnyAuthenticationManger extends AbstractAuthenticationManager {

    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers List of authentication handlers.
     */
    public AnyAuthenticationManger(final List<AuthenticationHandler> handlers ) {
        super(handlers);
    }

    /**
     * Creates a new authentication manager with a map of authentication handlers to the principal resolvers that
     * should be used upon successful authentication if no principal is resolved by the authentication handler. If
     * the order of evaluation of authentication handlers is important, a map that preserves insertion order
     * (e.g. {@link LinkedHashMap}) should be used.
     *
     * @param map A map of authentication handler to principal resolver.
     */
    public AnyAuthenticationManger(final Map<AuthenticationHandler, PrincipalResolver> map) {
        super(map);
    }

    @Override
    protected boolean isSatisfied(
            final List<Credential> credentials,
            final List<Credential> authenticated,
            final Authentication authentication) {
        return authentication.getSuccesses().size() > 0;
    }
}
