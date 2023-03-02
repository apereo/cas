package org.apereo.cas.authentication.principal;

import org.pac4j.core.client.BaseClient;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface DelegatedAuthenticationCredentialExtractor {

    /**
     * Extract client credential.
     *
     * @param client     the client
     * @param webContext the web context
     * @return the client credential
     */
    Optional<ClientCredential> extract(BaseClient client, RequestContext webContext);
}
