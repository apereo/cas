package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Retrieves entity statements within the request budget of the running resolution
 * ({@link OidcFederationResolutionLimiter}): each statement the resolution has not fetched yet uses one request of its
 * budget, a statement it fetched already is reused, and a resolution that has used up its budget fails. Outside a
 * resolution, statements are retrieved as they are.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcFederationBudgetedEntityStatementRetriever implements EntityStatementRetriever {
    private final EntityStatementRetriever delegate;

    @Override
    public EntityStatement fetchEntityConfiguration(final EntityID target) throws ResolveException {
        return fetch("configuration " + target, () -> delegate.fetchEntityConfiguration(target));
    }

    @Override
    public EntityStatement fetchEntityStatement(final URI federationAPIEndpoint, final EntityID issuer,
                                                final EntityID subject)
            throws ResolveException {
        return fetch("statement " + issuer + ' ' + subject + ' ' + federationAPIEndpoint,
            () -> delegate.fetchEntityStatement(federationAPIEndpoint, issuer, subject));
    }

    private static EntityStatement fetch(final String key, final Fetch fetch) throws ResolveException {
        val budget = OidcFederationResolutionLimiter.currentBudget();
        if (budget.isEmpty()) {
            return fetch.fetch();
        }
        val fetched = budget.get().getStatement(key);
        if (fetched != null) {
            return fetched;
        }
        if (!budget.get().tryConsume()) {
            throw new ResolveException("Federation resolution exceeds %s requests; %s is not fetched"
                .formatted(budget.get().getMaxRequests(), key));
        }
        val statement = fetch.fetch();
        budget.get().putStatement(key, statement);
        return statement;
    }

    @FunctionalInterface
    private interface Fetch {
        EntityStatement fetch() throws ResolveException;
    }
}
