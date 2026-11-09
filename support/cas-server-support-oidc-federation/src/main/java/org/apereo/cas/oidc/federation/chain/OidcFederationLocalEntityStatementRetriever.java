package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.ResolveException;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedSupplier;

/**
 * Retrieves entity statements while resolving trust chains on behalf of this entity: the entity configuration of this
 * entity and the subordinate statements it issues are built locally, as its endpoints would publish them, instead of
 * requested from itself; every other statement is retrieved by the delegate.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcFederationLocalEntityStatementRetriever implements EntityStatementRetriever {
    private final EntityStatementRetriever delegate;

    private final EntityID entityId;

    private final CheckedSupplier<EntityStatement> entityConfiguration;

    private final CheckedFunction<EntityID, Optional<EntityStatement>> subordinateStatements;

    @Override
    public EntityStatement fetchEntityConfiguration(final EntityID target) throws ResolveException {
        if (!entityId.equals(target)) {
            return delegate.fetchEntityConfiguration(target);
        }
        try {
            return entityConfiguration.get();
        } catch (final Throwable e) {
            throw new ResolveException("Entity configuration of [%s] cannot be built: %s".formatted(entityId, e.getMessage()), e);
        }
    }

    @Override
    public EntityStatement fetchEntityStatement(final URI federationAPIEndpoint, final EntityID issuer,
                                                final EntityID subject)
            throws ResolveException {
        if (!entityId.equals(issuer)) {
            return delegate.fetchEntityStatement(federationAPIEndpoint, issuer, subject);
        }
        try {
            return subordinateStatements.apply(subject)
                .orElseThrow(() -> new ResolveException("[%s] is not a subordinate of [%s]".formatted(subject, entityId)));
        } catch (final ResolveException e) {
            throw e;
        } catch (final Throwable e) {
            throw new ResolveException("Subordinate statement of [%s] about [%s] cannot be built: %s"
                .formatted(entityId, subject, e.getMessage()), e);
        }
    }
}
