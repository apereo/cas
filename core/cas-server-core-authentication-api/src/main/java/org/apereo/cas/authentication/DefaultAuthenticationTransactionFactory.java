package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serial;
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
@RequiredArgsConstructor
public class DefaultAuthenticationTransactionFactory implements AuthenticationTransactionFactory {
    @Serial
    private static final long serialVersionUID = -3106762590844787854L;

    protected final ServicesManager servicesManager;

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
        return new HashSet<>();
    }

    @Override
    public AuthenticationTransaction newTransaction(final Service service, final Credential... credentials) {
        val credentialSet = sanitizeCredentials(credentials);
        val registeredService = servicesManager.findServiceBy(service);
        return new DefaultAuthenticationTransaction(service, registeredService, credentialSet);
    }
}
