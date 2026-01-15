package org.apereo.cas.support.oauth.web.response.accesstoken;

import module java.base;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

/**
 * This is {@link OAuth20TokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface OAuth20TokenGenerator {

    /**
     * Generate access token and add it to the registry.
     *
     * @param tokenRequestContext the response holder
     * @return the token result
     * @throws Throwable the throwable
     */
    OAuth20TokenGeneratedResult generate(AccessTokenRequestContext tokenRequestContext) throws Throwable;
}
