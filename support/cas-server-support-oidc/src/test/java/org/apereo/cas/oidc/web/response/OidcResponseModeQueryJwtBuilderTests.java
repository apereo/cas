package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.token.JwtBuilder;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
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
public class OidcResponseModeQueryJwtBuilderTests {

    @TestPropertySource(properties = {
        "cas.authn.oidc.response.crypto.signing-enabled=false",
        "cas.authn.oidc.response.crypto.encryption-enabled=false"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DisabledTests extends AbstractOidcTests {
        @Test
        public void verifyResponseModeWithoutCrypto() throws Exception {
            val registeredService = getOidcRegisteredService("client").setJwks(StringUtils.EMPTY);
            servicesManager.save(registeredService);
            val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.QUERY_JWT);
            assertNotNull(builder);
            val mv = builder.build(registeredService, "https://apereo.github.io",
                Map.of("code", "123456", "state", "abcdef"));
            val response = mv.getModel().get("response").toString();
            val claims = JwtBuilder.parse(response);
            assertNotNull(claims.getClaim("code"));
            assertNotNull(claims.getClaim("state"));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends AbstractOidcTests {
        @Test
        public void verifyResponseModeForService() throws Exception {
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

        @Test
        public void verifyResponseModeForDefault() throws Exception {
            val registeredService = getOidcRegisteredService("client").setJwks(StringUtils.EMPTY);
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
}
