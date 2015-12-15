package org.jasig.cas.authentication;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationTransactionFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationTransactionFactory")
public class DefaultAuthenticationTransactionFactory implements AuthenticationTransactionFactory {
    @Override
    public AuthenticationTransaction get(final Credential... credentials) {
        final Set<Credential> sanitizedCredentials = sanitizeCredentials(credentials);
        return new DefaultAuthenticationTransaction(sanitizedCredentials);
    }

    /**
     * Attempts to sanitize the array of credentials by removing
     * all null elements from the collection.
     *
     * @param credentials credentials to sanitize
     * @return a set of credentials with no null values
     */
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
