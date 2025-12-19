package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcResponseModeFragmentJwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
class OidcResponseModeFragmentJwtBuilderTests extends AbstractOidcTests {
    @Test
    void verifyResponseModeForService() throws Throwable {
        val registeredService = getOidcRegisteredService("client");
        servicesManager.save(registeredService);
        val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.FRAGMENT_JWT);
        assertNotNull(builder);
        val mv = builder.build(registeredService, "https://apereo.github.io",
            Map.of("code", "123456", "state", "abcdef"));
        assertInstanceOf(RedirectView.class, mv.getView());
        val view = (RedirectView) mv.getView();
        val urlBuilder = new URIBuilder(view.getUrl());
        assertTrue(urlBuilder.getFragment().startsWith("response="));
    }

    @Test
    void verifyResponseModeForDefault() throws Throwable {
        val registeredService = getOidcRegisteredService("client").setJwks(StringUtils.EMPTY);
        servicesManager.save(registeredService);
        val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.FRAGMENT_JWT);
        assertNotNull(builder);
        val mv = builder.build(registeredService, "https://apereo.github.io",
            Map.of("code", "123456", "state", "abcdef"));
        assertInstanceOf(RedirectView.class, mv.getView());
        val view = (RedirectView) mv.getView();
        val urlBuilder = new URIBuilder(view.getUrl());
        assertTrue(urlBuilder.getFragment().startsWith("response="));
    }
}
