package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.NamedObject;
import org.pac4j.core.client.BaseClient;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface DelegatedAuthenticationCredentialExtractor extends Ordered, NamedObject {

    /**
     * Extract client credential.
     *
     * @param client         the client
     * @param requestContext the web context
     * @return the client credential
     */
    Optional<ClientCredential> extract(BaseClient client, RequestContext requestContext);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
