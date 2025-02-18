package org.apereo.cas.oidc.issuer;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultIssuerServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
class OidcDefaultIssuerServiceTests {
    protected static JEEContext getContextForEndpoint(final String endpoint) {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(8443);
        request.setRequestURI("/cas/oidc/" + endpoint);
        val response = new MockHttpServletResponse();
        return new JEEContext(request, response);
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.accepted-issuers-pattern=https:..sso.example.org.*")
    class BasicTests extends AbstractOidcTests {
        @Test
        void verifyOperation() {
            assertNotNull(oidcIssuerService.determineIssuer(Optional.empty()));
        }

        @Test
        void verifyEchoingOperation() {
            val svc = getOidcRegisteredService();
            val oidcService = OidcIssuerService.echoing("https://custom.issuer/");
            assertEquals("https://custom.issuer/", oidcService.determineIssuer(Optional.empty()));
            assertEquals("https://custom.issuer/", oidcService.determineIssuer(Optional.of(svc)));

            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = new JEEContext(request, response);
            assertTrue(oidcService.validateIssuer(context, OidcConstants.CLIENT_CONFIGURATION_URL));
        }

        @Test
        void verifyServiceIssuer() {
            val svc = getOidcRegisteredService();
            var issuer = oidcIssuerService.determineIssuer(Optional.of(svc));
            assertEquals(issuer, casProperties.getAuthn().getOidc().getCore().getIssuer());
            svc.setIdTokenIssuer("https://custom.issuer/");
            issuer = oidcIssuerService.determineIssuer(Optional.of(svc));
            assertEquals("https://custom.issuer", issuer);
        }

        @Test
        void verifyIssuerPatterns() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("profile"), "profile"));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/oidc")
    class StaticIssuer extends AbstractOidcTests {
        @Test
        void validateStaticIssuer() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("authorize"), "authorize"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("profile"), "profile"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), "logout"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("realms/authorize"), "authorize"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), "oidcLogout"));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/oidc/custom/fawnoos/issuer")
    class DynamicIssuer extends AbstractOidcTests {
        @Test
        void validateDynamicIssuer() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/authorize"), "authorize"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/profile"), "profile"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/oidcAuthorize"), "oidcAuthorize"));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer"), "unknown"));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/openid-connect")
    class MismatchedIssuer extends AbstractOidcTests {
        @Test
        void validateIssuerMismatch() {
            assertFalse(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), "oidcLogout"));
        }
    }
}
