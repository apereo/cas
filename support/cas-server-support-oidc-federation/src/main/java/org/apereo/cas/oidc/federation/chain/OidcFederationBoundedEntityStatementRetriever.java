package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Retrieves entity statements while resolving trust chains, refusing any entity configuration that lists more
 * authority hints than allowed. OpenID Federation 1.0, section 18.1, recommends such a limit: every authority hint is
 * another request, so an entity that lists many of them, at any level of the chain, could otherwise make CAS send any
 * number of requests for a single authorization request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcFederationBoundedEntityStatementRetriever implements EntityStatementRetriever {
    private final EntityStatementRetriever delegate;

    private final int maxAuthorityHints;

    @Override
    public EntityStatement fetchEntityConfiguration(final EntityID target) throws ResolveException {
        val entityConfiguration = delegate.fetchEntityConfiguration(target);
        val authorityHints = entityConfiguration.getClaimsSet().getAuthorityHints();
        if (authorityHints != null && authorityHints.size() > maxAuthorityHints) {
            throw new ResolveException("Entity [%s] lists [%s] authority hints, more than the [%s] that are followed"
                .formatted(target, authorityHints.size(), maxAuthorityHints));
        }
        return entityConfiguration;
    }

    @Override
    public EntityStatement fetchEntityStatement(final URI federationAPIEndpoint, final EntityID issuer,
                                                final EntityID subject)
            throws ResolveException {
        return delegate.fetchEntityStatement(federationAPIEndpoint, issuer, subject);
    }
}
