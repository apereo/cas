package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcResponseModeFormPostJwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
class OidcResponseModeFormPostJwtBuilderTests {

    @Nested
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyResponseModeForService() throws Throwable {
            val registeredService = getOidcRegisteredService("client");
            servicesManager.save(registeredService);
            val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.FORM_POST_JWT);
            assertNotNull(builder);
            val mv = builder.build(registeredService, "https://apereo.github.io",
                Map.of("code", "123456", "state", "abcdef"));
            assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
            assertEquals("https://apereo.github.io", mv.getModelMap().get("originalUrl"));
            val params = (Map) mv.getModelMap().get("parameters");
            assertEquals(1, params.size());
            assertTrue(params.containsKey("response"));
        }

        @Test
        void verifyResponseModeForDefault() throws Throwable {
            val registeredService = getOidcRegisteredService("client").setJwks(StringUtils.EMPTY);
            servicesManager.save(registeredService);
            val builder = oauthResponseModeFactory.getBuilder(registeredService, OAuth20ResponseModeTypes.FORM_POST_JWT);
            assertNotNull(builder);
            val mv = builder.build(registeredService, "https://apereo.github.io",
                Map.of("code", "123456", "state", "abcdef"));
            assertEquals(CasWebflowConstants.VIEW_ID_POST_RESPONSE, mv.getViewName());
            assertEquals("https://apereo.github.io", mv.getModelMap().get("originalUrl"));
            val params = (Map) mv.getModelMap().get("parameters");
            assertEquals(1, params.size());
            assertTrue(params.containsKey("response"));
        }
    }
}
