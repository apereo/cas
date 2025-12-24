package org.apereo.cas.authentication.principal;

import module java.base;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface DelegatedClientAuthenticationCredentialResolver {
    /**
     * Supports this client credential?
     *
     * @param credentials the credentials
     * @return true/false
     */
    boolean supports(ClientCredential credentials);

    /**
     * Resolve list of candidate profiles.
     *
     * @param context     the context
     * @param credentials the credentials
     * @return the list
     * @throws Throwable the throwable
     */
    @Nullable List<DelegatedAuthenticationCandidateProfile> resolve(RequestContext context, ClientCredential credentials) throws Throwable;
}
