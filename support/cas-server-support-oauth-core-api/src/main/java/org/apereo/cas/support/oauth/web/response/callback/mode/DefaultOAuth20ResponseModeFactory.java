package org.apereo.cas.support.oauth.web.response.callback.mode;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;

/**
 * This is {@link DefaultOAuth20ResponseModeFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DefaultOAuth20ResponseModeFactory implements OAuth20ResponseModeFactory {
    @Override
    public OAuth20ResponseModeBuilder getBuilder(final RegisteredService registeredService,
                                                 final OAuth20ResponseModeTypes responseMode) {
        if (OAuth20ResponseModeFactory.isResponseModeTypeFormPost(registeredService, responseMode)) {
            return new OAuth20ResponseModeFormPostBuilder();
        }
        if (OAuth20ResponseModeFactory.isResponseModeTypeFragment(registeredService, responseMode)) {
            return new OAuth20ResponseModeFragmentBuilder();
        }
        return new OAuth20ResponseModeQueryBuilder();
    }
}
