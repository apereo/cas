package org.apereo.cas.authentication.surrogate;

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
    public boolean canAuthenticateAs(final String surrogate, final Principal principal) {
        if (this.eligibleAccounts.containsKey(principal.getId())) {
            final Set surrogates = this.eligibleAccounts.get(principal.getId());
            return surrogates.contains(surrogate);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        return this.eligibleAccounts.get(username);
    }
}
