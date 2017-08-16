package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSurrogateAuthenticationService.class);
    
    private final Map<String, Set> eligibleAccounts;

    /**
     * Instantiates a new simple surrogate username password service.
     *
     * @param eligibleAccounts the eligible accounts
     */
    public SimpleSurrogateAuthenticationService(final Map<String, Set> eligibleAccounts) {
        this.eligibleAccounts = eligibleAccounts;
    }

    @Override
    public boolean canAuthenticateAs(final String surrogate, final Principal principal) {
        if (this.eligibleAccounts.containsKey(principal.getId())) {
            final Set surrogates = this.eligibleAccounts.get(principal.getId());
            LOGGER.debug("Surrogate accounts authorized for [{}] are [{}]", principal.getId(), surrogates);
            return surrogates.contains(surrogate);
        }
        LOGGER.warn("[{}] is not eligible to authenticate as [{}]", principal.getId(), surrogate);
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        return this.eligibleAccounts.get(username);
    }
}
