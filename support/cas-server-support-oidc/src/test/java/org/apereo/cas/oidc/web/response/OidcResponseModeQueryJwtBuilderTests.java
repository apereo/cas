package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcResponseModeQueryJwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
public class OidcResponseModeQueryJwtBuilderTests extends AbstractOidcTests {
    @Test
    public void verifyResponseMode() throws Exception {
        val registeredService = getOidcRegisteredService("client");
        servicesManager.save(registeredService);
        val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.QUERY_JWT);
        assertNotNull(builder);
        val mv = builder.build(registeredService, "https://apereo.github.io",
            Map.of("code", "123456", "state", "abcdef"));
        assertTrue(mv.getView() instanceof RedirectView);
        val view = (RedirectView) mv.getView();
        assertEquals("https://apereo.github.io", view.getUrl());
        assertTrue(mv.getModel().containsKey("response"));
        assertEquals(OAuth20ResponseModeTypes.QUERY_JWT, builder.getResponseMode());
    }
}
