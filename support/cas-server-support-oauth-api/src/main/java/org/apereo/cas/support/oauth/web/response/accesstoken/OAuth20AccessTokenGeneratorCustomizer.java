package org.apereo.cas.support.oauth.web.response.accesstoken;

import module java.base;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20AccessTokenGeneratorCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OAuth20AccessTokenGeneratorCustomizer extends Ordered {
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Customize.
     *
     * @param tokenRequestContext the token request context
     * @param accessToken         the access token
     */
    void customize(AccessTokenRequestContext tokenRequestContext, OAuth20AccessToken accessToken);
}
