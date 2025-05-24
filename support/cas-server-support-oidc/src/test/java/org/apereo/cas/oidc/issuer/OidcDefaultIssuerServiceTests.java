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
import java.util.List;
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
            val registeredService = getOidcRegisteredService();
            val oidcService = OidcIssuerService.echoing("https://custom.issuer/");
            assertEquals("https://custom.issuer/", oidcService.determineIssuer(Optional.empty()));
            assertEquals("https://custom.issuer/", oidcService.determineIssuer(Optional.of(registeredService)));

            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = new JEEContext(request, response);
            assertTrue(oidcService.validateIssuer(context, List.of(OidcConstants.CLIENT_CONFIGURATION_URL)));
        }

        @Test
        void verifyServiceIssuer() {
            val registeredService = getOidcRegisteredService();
            var issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            assertEquals(issuer, casProperties.getAuthn().getOidc().getCore().getIssuer());
            registeredService.setIdTokenIssuer("https://custom.issuer/");
            issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            assertEquals("https://custom.issuer", issuer);
        }

        @Test
        void verifyIssuerPatterns() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("profile"), List.of("profile")));
        }

        @Test
        void validateServiceIssuer() {
            val registeredService = getOidcRegisteredService();
            registeredService.setIdTokenIssuer("https://custom.issuer/");
            var issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            assertEquals("https://custom.issuer", issuer);
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("authorize"), List.of("authorize"), registeredService));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/oidc")
    class StaticIssuer extends AbstractOidcTests {
        @Test
        void validateStaticIssuer() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("authorize"), List.of("authorize")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("profile"), List.of("profile")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), List.of("logout")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("realms/authorize"), List.of("authorize")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), List.of("oidcLogout")));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/oidc/custom/fawnoos/issuer")
    class DynamicIssuer extends AbstractOidcTests {
        @Test
        void validateDynamicIssuer() {
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/authorize"), List.of("authorize")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/profile"), List.of("profile")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer/oidcAuthorize"), List.of("oidcAuthorize")));
            assertTrue(oidcIssuerService.validateIssuer(getContextForEndpoint("custom/fawnoos/issuer"), List.of("unknown")));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://sso.example.org:8443/cas/openid-connect")
    class MismatchedIssuer extends AbstractOidcTests {
        @Test
        void validateIssuerMismatch() {
            assertFalse(oidcIssuerService.validateIssuer(getContextForEndpoint("logout"), List.of("oidcLogout")));
        }
    }
}
