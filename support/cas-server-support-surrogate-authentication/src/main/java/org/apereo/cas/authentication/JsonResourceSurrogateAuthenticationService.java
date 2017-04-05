package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.apereo.cas.authentication.principal.Principal;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link JsonResourceSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JsonResourceSurrogateAuthenticationService implements SurrogateAuthenticationService {
    private final Map<String, List> eligibleAccounts;

    /**
     * Instantiates a new simple surrogate username password service.
     */
    public JsonResourceSurrogateAuthenticationService(final Resource json) {
        try {
            final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            this.eligibleAccounts = mapper.readValue(json.getFile(), Map.class);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
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
