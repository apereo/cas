package org.apereo.cas.support.oauth.web.response.callback.mode;

import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DefaultOAuth20ResponseModeFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DefaultOAuth20ResponseModeFactory implements OAuth20ResponseModeFactory {
    private final Map<String, OAuth20ResponseModeBuilder> builders = new LinkedHashMap<>();

    @Override
    @CanIgnoreReturnValue
    public OAuth20ResponseModeFactory registerBuilder(final OAuth20ResponseModeBuilder builder) {
        builders.put(builder.getResponseMode().getType(), builder);
        return this;
    }

    @Override
    public OAuth20ResponseModeBuilder getBuilder(final OAuthRegisteredService registeredService,
                                                 final OAuth20ResponseModeTypes responseMode) {
        if (OAuth20ResponseModeFactory.isResponseModeTypeFormPost(registeredService, responseMode)) {
            return builders.get(OAuth20ResponseModeTypes.FORM_POST.getType());
        }
        if (OAuth20ResponseModeFactory.isResponseModeTypeFragment(registeredService, responseMode)) {
            return builders.get(OAuth20ResponseModeTypes.FRAGMENT.getType());
        }
        if (OAuth20ResponseModeFactory.isResponseModeTypeFormPostJwt(registeredService, responseMode)) {
            return builders.get(OAuth20ResponseModeTypes.FORM_POST_JWT.getType());
        }
        if (OAuth20ResponseModeFactory.isResponseModeTypeQueryJwt(registeredService, responseMode)) {
            return builders.get(OAuth20ResponseModeTypes.QUERY_JWT.getType());
        }
        if (OAuth20ResponseModeFactory.isResponseModeTypeFragmentJwt(registeredService, responseMode)) {
            return builders.get(OAuth20ResponseModeTypes.FRAGMENT_JWT.getType());
        }
        return builders.get(OAuth20ResponseModeTypes.QUERY.getType());
    }
}
