package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link OAuth20ProofOfPossessionValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OAuth20ProofOfPossessionValidator {

    /**
     * Validate.
     *
     * @param webContext  the web context
     * @param accessToken the access token
     * @throws Throwable the throwable
     */
    void validate(WebContext webContext, @Nullable OAuth20AccessToken accessToken) throws Throwable;

    /**
     * Validate.
     *
     * @param webContext the web context
     * @throws Throwable the throwable
     */
    default void validate(final WebContext webContext) throws Throwable {
        validate(webContext, null);
    }
}
