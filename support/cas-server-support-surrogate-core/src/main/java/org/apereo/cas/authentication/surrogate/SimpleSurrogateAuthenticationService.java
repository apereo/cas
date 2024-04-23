package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SimpleSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class SimpleSurrogateAuthenticationService extends BaseSurrogateAuthenticationService {
    private final Map<String, List> eligibleAccounts;

    public SimpleSurrogateAuthenticationService(final Map<String, List> eligibleAccounts, final ServicesManager servicesManager) {
        super(servicesManager);
        this.eligibleAccounts = eligibleAccounts;
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<? extends Service> service) {
        if (this.eligibleAccounts.containsKey(principal.getId())) {
            val surrogates = this.eligibleAccounts.get(principal.getId());
            LOGGER.debug("Surrogate accounts authorized for [{}] are [{}]", principal.getId(), surrogates);
            return surrogates.contains(surrogate);
        }
        LOGGER.warn("[{}] is not eligible to authenticate as [{}]", principal.getId(), surrogate);
        return false;
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) {
        if (this.eligibleAccounts.containsKey(username)) {
            return this.eligibleAccounts.get(username);
        }
        return new ArrayList<>(0);
    }
}
