package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20ResponseModeFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OAuth20ResponseModeFactory {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oauthResponseModeFactory";

    /**
     * Is response mode type form post?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    static boolean isResponseModeTypeFormPostJwt(final OAuthRegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FORM_POST_JWT
               || (registeredService != null
                   && StringUtils.equalsIgnoreCase(OAuth20ResponseModeTypes.FORM_POST_JWT.getType(), registeredService.getResponseMode()));
    }

    /**
     * Is response mode type form post?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    static boolean isResponseModeTypeFormPost(final OAuthRegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FORM_POST
               || (registeredService != null
                   && (StringUtils.equalsIgnoreCase("post", registeredService.getResponseMode())
                       || StringUtils.equalsIgnoreCase(OAuth20ResponseModeTypes.FORM_POST.getType(), registeredService.getResponseMode())));
    }

    /**
     * Is response mode type fragment?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    static boolean isResponseModeTypeFragment(final OAuthRegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FRAGMENT
               || (registeredService != null && StringUtils.equalsIgnoreCase(
            OAuth20ResponseModeTypes.FRAGMENT.getType(), registeredService.getResponseMode()));
    }

    /**
     * Is response mode type fragment jwt?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    static boolean isResponseModeTypeFragmentJwt(final OAuthRegisteredService registeredService,
                                                 final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FRAGMENT_JWT
               || (registeredService != null && StringUtils.equalsIgnoreCase(
            OAuth20ResponseModeTypes.FRAGMENT_JWT.getType(), registeredService.getResponseMode()));
    }

    /**
     * Is response mode type query jwt?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    static boolean isResponseModeTypeQueryJwt(final OAuthRegisteredService registeredService,
                                              final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.QUERY_JWT
               || (registeredService != null && StringUtils.equalsIgnoreCase(
            OAuth20ResponseModeTypes.QUERY_JWT.getType(), registeredService.getResponseMode()));
    }

    /**
     * Register builder for mode factory.
     *
     * @param builder the builder
     * @return the oauth response mode factory
     */
    OAuth20ResponseModeFactory registerBuilder(OAuth20ResponseModeBuilder builder);

    /**
     * Gets builder.
     *
     * @param registeredService the registered service
     * @param type              the type
     * @return the builder
     */
    OAuth20ResponseModeBuilder getBuilder(OAuthRegisteredService registeredService, OAuth20ResponseModeTypes type);
}
