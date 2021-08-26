package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationTransactionFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class DefaultAuthenticationTransactionFactory implements AuthenticationTransactionFactory {
    private static final long serialVersionUID = -3106762590844787854L;

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

    @Override
    public AuthenticationTransaction newTransaction(final Service service, final Credential... credentials) {
        val creds = sanitizeCredentials(credentials);
        return new DefaultAuthenticationTransaction(service, creds);
    }
}
