package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SimpleSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SimpleSurrogateAuthenticationService implements SurrogateAuthenticationService {
    private final Map<String, Set> eligibleAccounts;

    /**
     * Instantiates a new simple surrogate username password service.
     */
    public SimpleSurrogateAuthenticationService(final Map<String, Set> eligibleAccounts) {
        this.eligibleAccounts = eligibleAccounts;
    }

    @Override
    public boolean canAuthenticateAs(final String username, final Principal surrogate) {
        return this.eligibleAccounts.containsKey(username);
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        return this.eligibleAccounts.get(username);
    }
}
