package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationSupervisor")
public final class DefaultAuthenticationTransactionManager implements AuthenticationTransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationTransactionManager.class);

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("defaultAuthenticationContextBuilder")
    private AuthenticationContextBuilder authenticationContextBuilder;

    /**
     * Instantiates a new Default authentication supervisor.
     */
    public DefaultAuthenticationTransactionManager() {}

    @Override
    public AuthenticationContextBuilder processAuthenticationAttempt(final Credential... credentials) throws AuthenticationException {
        final Set<Credential> sanitizedCredentials = sanitizeCredentials(credentials);
        if (!sanitizedCredentials.isEmpty()) {
            final Credential[] sanitizedCredentialsArray = sanitizedCredentials.toArray(new Credential[] {});
            LOGGER.debug("Sanitized credentials [{}] prior to authentication", sanitizedCredentialsArray);
            final Authentication authentication =
                    this.authenticationManager.authenticate(sanitizedCredentialsArray);
            LOGGER.debug("Successful authentication; Collecting authentication result [{}]", authentication);
            this.authenticationContextBuilder.collect(authentication);
        } else {
            LOGGER.debug("No credentials were provided for authentication");
        }
        return authenticationContextBuilder;
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

    /**
     * Sets authentication manager.
     *
     * @param authenticationManager the authentication manager
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Sets authentication context builder.
     *
     * @param authenticationContextBuilder the authentication context builder
     */
    public void setAuthenticationContextBuilder(final AuthenticationContextBuilder authenticationContextBuilder) {
        this.authenticationContextBuilder = authenticationContextBuilder;
    }
}
