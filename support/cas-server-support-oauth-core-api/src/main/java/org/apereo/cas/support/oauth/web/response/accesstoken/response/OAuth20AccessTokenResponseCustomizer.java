package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import module java.base;

/**
 * This is {@link OAuth20AccessTokenResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OAuth20AccessTokenResponseCustomizer {

    /**
     * Customize the access token response.
     *
     * @param result the result
     * @param model  the model
     * @return the map
     */
    default Map<String, Object> customize(final OAuth20AccessTokenResponseResult result,
                                          final Map<String, Object> model) {
        return model;
    }
}
