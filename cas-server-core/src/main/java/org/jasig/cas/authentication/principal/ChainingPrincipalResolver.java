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

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.Credential;

/**
 * Delegates to one or more principal resolves in series to resolve a principal. The input to first configured resolver
 * is the authenticated credential; for every subsequent resolver, the input is a {@link Credential} whose ID is the
 * resolved princpial ID of the previous resolver.
 * <p>
 * A common use case for this component is resolving a temporary principal ID from an X.509 credential followed by
 * a search (e.g. LDAP, database) for the final principal based on the temporary ID.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class ChainingPrincipalResolver implements PrincipalResolver {

    /** The chain of delegate resolvers that are invoked in order. */
    @NotNull
    @Size(min = 1)
    private List<PrincipalResolver> chain;

    /**
     * Sets the resolver chain. The resolvers other than the first one MUST be capable of performing resolution
     * on the basis of {@link org.jasig.cas.authentication.Credential#getId()} alone;
     * {@link PersonDirectoryPrincipalResolver} notably meets that requirement.
     *
     * @param chain List of delegate resolvers that are invoked in a chain.
     */
    public void setChain(final List<PrincipalResolver> chain) {
        this.chain = chain;
    }

    /**
     * Resolves a credential by delegating to each of the configured resolvers in sequence. Note that the
     * {@link PrincipalResolver#supports(org.jasig.cas.authentication.Credential)} method is called only for the
     * first configured resolver.
     *
     * @param credential Authenticated credential.
     *
     * @return The principal from the last configured resolver in the chain.
     */
    public Principal resolve(final Credential credential) {
        Principal result = null;
        Credential input = credential;
        for (final PrincipalResolver resolver : this.chain) {
            if (result != null) {
                input = new IdentifiableCredential(result.getId());
            }
            result = resolver.resolve(input);
        }
        return result;
    }

    /**
     * Determines whether the credential is supported by this component by delegating to the first configured
     * resolver in the chain.
     *
     * @param credential The credential to check for support.
     *
     * @return True if the first configured resolver in the chain supports the credential, false otherwise.
     */
    public boolean supports(final Credential credential) {
        return this.chain.get(0).supports(credential);
    }

    /** Credential that stores only an ID. */
    static class IdentifiableCredential implements Credential {
        /** Credential identifier. */
        private final String id;

        /** Creates a new instance with the given ID. */
        public IdentifiableCredential(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }
}
