package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.pac4j.core.client.BaseClient;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface DelegatedAuthenticationPreProcessor extends Ordered {
    /**
     * Process principal.
     *
     * @param principal  the principal
     * @param client     the client
     * @param credential the credential
     * @param service    the service
     * @return the principal
     * @throws Throwable the throwable
     */
    Principal process(Principal principal, BaseClient client, Credential credential, Service service) throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
