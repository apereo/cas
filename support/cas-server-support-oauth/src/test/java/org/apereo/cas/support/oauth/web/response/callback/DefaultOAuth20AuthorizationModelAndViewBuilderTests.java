package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20AuthorizationModelAndViewBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20AuthorizationModelAndViewBuilderTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthAuthorizationModelAndViewBuilder")
    private OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder;

    @Test
    void verifyFragment() throws Throwable {
        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        servicesManager.save(registeredService);
        val mv = oauthAuthorizationModelAndViewBuilder.build(registeredService, OAuth20ResponseModeTypes.FRAGMENT,
            "https://github.com/apereo/cas?one=value&two=value&code=123456",
            Map.of("code", "123456"));
        assertInstanceOf(RedirectView.class, mv.getView());
        val view = (RedirectView) mv.getView();
        assertTrue(mv.getModel().isEmpty());
        assertEquals("code=123456", new URI(view.getUrl()).getFragment());

    }
}
