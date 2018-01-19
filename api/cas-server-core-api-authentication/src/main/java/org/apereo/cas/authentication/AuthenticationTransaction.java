package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class AuthenticationTransaction implements Serializable {

    private static final long serialVersionUID = 6213904009424725484L;

    private final Service service;
    private final Collection<Credential> credentials;

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

    /**
     * Sanitize credentials set. It's important to keep the order of
     * the credentials in the final set as they were presented.
     *
     * @param credentials the credentials
     * @return the set
     */
    private static Set<Credential> sanitizeCredentials(final Credential[] credentials) {
        if (credentials != null && credentials.length > 0) {
            return Arrays.stream(credentials)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return new HashSet<>(0);
    }
}

