package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20ResponseModeFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OAuth20ResponseModeFactory {
    String BEAN_NAME = "oauthResponseModeFactory";

    /**
     * Is response mode type form post?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return the boolean
     */
    static boolean isResponseModeTypeFormPost(final RegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FORM_POST
               || (registeredService != null && StringUtils.equalsIgnoreCase("post", registeredService.getResponseType()));
    }

    /**
     * Is response mode type fragment?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return the boolean
     */
    static boolean isResponseModeTypeFragment(final RegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FRAGMENT
               || (registeredService != null && StringUtils.equalsIgnoreCase(
            OAuth20ResponseModeTypes.FRAGMENT.getType(), registeredService.getResponseType()));
    }

    /**
     * Is response mode type query jwt?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return the boolean
     */
    static boolean isResponseModeTypeQueryJwt(final RegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.QUERY_JWT
               || (registeredService != null && StringUtils.equalsIgnoreCase(
            OAuth20ResponseModeTypes.QUERY_JWT.getType(), registeredService.getResponseType()));
    }

    OAuth20ResponseModeFactory registerBuilder(OAuth20ResponseModeBuilder builder);

    /**
     * Gets builder.
     *
     * @param registeredService the registered service
     * @param type              the type
     * @return the builder
     */
    OAuth20ResponseModeBuilder getBuilder(RegisteredService registeredService, OAuth20ResponseModeTypes type);
}
