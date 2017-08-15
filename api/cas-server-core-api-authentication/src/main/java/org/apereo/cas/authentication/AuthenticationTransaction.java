package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class AuthenticationTransaction implements Serializable {

    private static final long serialVersionUID = 6213904009424725484L;

    private final Collection<Credential> credentials;
    private final Service service;

    /**
     * Instantiates a new Default authentication transaction.
     *
     * @param service     the service
     * @param credentials the credentials
     */
    protected AuthenticationTransaction(final Service service, final Collection<Credential> credentials) {
        this.credentials = credentials;
        this.service = service;
    }

    public Collection<Credential> getCredentials() {
        return this.credentials;
    }

    /**
     * Wrap credentials into an authentication transaction, as a factory method,
     * and return the final result.
     *
     * @param service     the service
     * @param credentials the credentials
     * @return the authentication transaction
     */
    public static AuthenticationTransaction wrap(final Service service, final Credential... credentials) {
        return new AuthenticationTransaction(service, sanitizeCredentials(credentials));
    }

    /**
     * Wrap credentials into an authentication transaction, as a factory method,
     * and return the final result.
     *
     * @param credentials the credentials
     * @return the authentication transaction
     */
    public static AuthenticationTransaction wrap(final Credential... credentials) {
        return wrap(null, credentials);
    }

    public Service getService() {
        return this.service;
    }

    /**
     * Gets the first (primary) credential in the chain.
     *
     * @return the credential
     */
    public Credential getCredential() {
        if (!credentials.isEmpty()) {
            return credentials.iterator().next();
        }
        return null;
    }

    /**
     * Is credential of given type?
     *
     * @param clazz the clazz
     * @return true/false
     */
    public boolean isCredentialOfType(final Class clazz) {
        try {
            final Object object = clazz.cast(getCredential());
            return object != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private static Set<Credential> sanitizeCredentials(final Credential[] credentials) {
        if (credentials != null && credentials.length > 0) {
            return Arrays.stream(credentials)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }
}

