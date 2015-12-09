package org.jasig.cas.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationSupervisor}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationSupervisor")
public final class DefaultAuthenticationSupervisor implements AuthenticationSupervisor {

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("defaultAuthenticationContextBuilder")
    private AuthenticationContextBuilder authenticationContextBuilder;

    private DefaultAuthenticationSupervisor() {}

    @Override
    public boolean authenticate(final Credential... credentials) throws AuthenticationException {
        final Set<Credential> sanitizedCredentials = sanitizeCredentials(credentials);
        if (!sanitizedCredentials.isEmpty()) {
            final Authentication authentication =
                    this.authenticationManager.authenticate(sanitizedCredentials.toArray(new Credential[] {}));
            return this.authenticationContextBuilder.collect(authentication);
        }
        return false;
    }

    @Override
    public AuthenticationContext build() {
        return this.authenticationContextBuilder.build();
    }

    @Override
    public void clear() {
        this.authenticationContextBuilder.clear();
    }

    /**
     * Attempts to sanitize the array of credentials by removing
     * all null elements from the collection.
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
