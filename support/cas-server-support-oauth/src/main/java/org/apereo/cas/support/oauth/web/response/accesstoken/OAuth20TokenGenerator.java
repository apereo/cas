package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;

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
     * @param responseHolder the response holder
     * @return the access token
     */
    Pair<AccessToken, RefreshToken> generate(AccessTokenRequestDataHolder responseHolder);
}
