package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Credential;

import java.util.List;

/**
 * Delegates to one or more principal resolves in series to resolve a principal. The input to first configured resolver
 * is the authenticated credential; for every subsequent resolver, the input is a {@link Credential} whose ID is the
 * resolved principal ID of the previous resolver.
 * <p>
 * A common use case for this component is resolving a temporary principal ID from an X.509 credential followed by
 * a search (e.g. LDAP, database) for the final principal based on the temporary ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ChainingPrincipalResolver implements PrincipalResolver {

    /** The chain of delegate resolvers that are invoked in order. */
    private List<PrincipalResolver> chain;

    /**
     * Sets the resolver chain. The resolvers other than the first one MUST be capable of performing resolution
     * on the basis of {@link Credential#getId()} alone;
     * {@link PersonDirectoryPrincipalResolver} notably meets that requirement.
     *
     * @param chain List of delegate resolvers that are invoked in a chain.
     */
    public void setChain(final List<PrincipalResolver> chain) {
        this.chain = chain;
    }

    /**
     * Resolves a credential by delegating to each of the configured resolvers in sequence. Note that the
     * {@link PrincipalResolver#supports(Credential)} method is called only for the
     * first configured resolver.
     *
     * @param credential Authenticated credential.
     *
     * @return The principal from the last configured resolver in the chain.
     */
    @Override
    public Principal resolve(final Credential credential) {
        final Principal[] result = {null};
        final Credential[] input = {credential};
        this.chain.stream().forEach(resolver -> {
            if (result[0] != null) {
                input[0] = new IdentifiableCredential(result[0].getId());
            }
            result[0] = resolver.resolve(input[0]);
        });
        return result[0];
    }

    /**
     * Determines whether the credential is supported by this component by delegating to the first configured
     * resolver in the chain.
     *
     * @param credential The credential to check for support.
     *
     * @return True if the first configured resolver in the chain supports the credential, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return this.chain.get(0).supports(credential);
    }

    /** Credential that stores only an ID. */
    private static class IdentifiableCredential implements Credential {
        /** Credential identifier. */
        private String id;

        /**
         * Creates a new instance with the given ID.
         *
         * @param id the credential id
         */
        IdentifiableCredential(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }
}
