package org.jasig.cas.authentication;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class AuthenticationTransaction implements Serializable {

    private static final long serialVersionUID = 6213904009424725484L;

    private final Collection<Credential> credentials;

    /**
     * Instantiates a new Default authentication transaction.
     *
     * @param credentials the credentials
     */
    private AuthenticationTransaction(final Collection<Credential> credentials) {
        this.credentials = credentials;
    }

    public Collection<Credential> getCredentials() {
        return this.credentials;
    }

    /**
     * Wrap credentials into an authentication transaction, as a factory method,
     * and return the final result.
     *
     * @param credentials the credentials
     * @return the authentication transaction
     */
    public static AuthenticationTransaction wrap(final Credential... credentials) {
        return new AuthenticationTransaction(sanitizeCredentials(credentials));
    }

    private static Set<Credential> sanitizeCredentials(final Credential[] credentials) {
        if (credentials != null && credentials.length > 0) {
            final Set<Credential> set = new HashSet<>(Arrays.asList(credentials));
            final Iterator<Credential> it = set.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }
            return set;
        }
        return Collections.emptySet();
    }
}

