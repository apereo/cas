package org.apereo.cas.heimdall.engine;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link AuthorizationPrincipalParser}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface AuthorizationPrincipalParser {
    /**
     * Parse principal.
     *
     * @param authorizationHeader  the authorization header
     * @param authorizationRequest the authorization request
     * @return the principal
     * @throws Throwable the throwable
     */
    @Nullable Principal parse(String authorizationHeader, AuthorizationRequest authorizationRequest) throws Throwable;

    /**
     * Parse the caller while validating proofs against the actual HTTP request.
     *
     * @param authorizationHeader the authorization header
     * @param authorizationRequest the authorization request
     * @param webContext the HTTP context
     * @return the principal
     * @throws Throwable when authentication fails
     */
    default @Nullable Principal parse(final String authorizationHeader,
                                      final AuthorizationRequest authorizationRequest,
                                      final WebContext webContext)
            throws Throwable {
        return parse(authorizationHeader, authorizationRequest);
    }

    /**
     * Authenticate the caller of AuthZEN requests without resolving a subject, so that a batch of
     * evaluations is authenticated once.
     *
     * @param authorizationHeader the authorization header
     * @param webContext          the HTTP context
     * @throws Throwable when authentication fails
     */
    default void authenticateCaller(final String authorizationHeader, final WebContext webContext) throws Throwable {
        throw new UnsupportedOperationException("Authenticating AuthZEN callers is not supported by " + getClass().getSimpleName());
    }

    /**
     * Resolve the principal for an AuthZEN subject.
     *
     * @param subject the subject
     * @return the principal
     * @throws Throwable when the subject cannot be resolved
     */
    default @Nullable Principal resolveSubject(final AuthZenSubject subject) throws Throwable {
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(subject.getId());
    }
}
