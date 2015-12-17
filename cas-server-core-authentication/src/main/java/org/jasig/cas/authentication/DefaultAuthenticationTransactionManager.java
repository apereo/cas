package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * This is {@link DefaultAuthenticationTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationTransactionManager")
public final class DefaultAuthenticationTransactionManager implements AuthenticationTransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationTransactionManager.class);

    @NotNull
    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;

    @Override
    public AuthenticationTransactionManager handle(final AuthenticationTransaction authenticationTransaction,
                                                   final AuthenticationContextBuilder authenticationContext)
                                                    throws AuthenticationException {
        final Collection<Credential> sanitizedCredentials = authenticationTransaction.getCredentials();
        if (!sanitizedCredentials.isEmpty()) {
            final Credential[] sanitizedCredentialsArray = sanitizedCredentials.toArray(new Credential[] {});
            final Authentication authentication = this.authenticationManager.authenticate(sanitizedCredentialsArray);
            LOGGER.debug("Successful authentication; Collecting authentication result [{}]", authentication);
            authenticationContext.collect(authentication);
        } else {
            LOGGER.info("No credentials were provided for authentication. Authentication event is ignored and nothing is collected into " +
                    "the authentication context");
        }
        return this;
    }

    /**
     * Sets authentication manager.
     *
     * @param authenticationManager the authentication manager
     */
    @Override
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}
