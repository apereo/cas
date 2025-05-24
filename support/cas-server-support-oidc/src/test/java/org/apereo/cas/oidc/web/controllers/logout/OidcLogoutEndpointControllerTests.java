package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * This is {@link OidcLogoutEndpointControllerTests}.
 *
 * @author Julien Huon
 * @since 6.1.0
 */
@Tag("OIDCWeb")
class OidcLogoutEndpointControllerTests {

    @Nested
    class DefaultTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcProtocolEndpointConfigurer")
        private CasWebSecurityConfigurer<HttpSecurity> oidcProtocolEndpointConfigurer;

        @Test
        void verifyEndpoints() {
            assertFalse(oidcProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
        }

        @Test
        void verifyBadEndpointRequest() throws Exception {
            mockMvc.perform(get("/oidc/logout")).andExpect(status().isBadRequest());
        }

        @Test
        void verifyOidcNoLogoutUrls() throws Throwable {
            val id = UUID.randomUUID().toString();
            val claims = getClaims(id);
            val oidcRegisteredService = new OidcRegisteredService();
            oidcRegisteredService.setClientId(id);
            oidcRegisteredService.setServiceId("https://example.org");
            oidcRegisteredService.setName(id);
            servicesManager.save(oidcRegisteredService);
            val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

            mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT));
        }

        @Test
        void verifyOidcLogoutWithoutParams() throws Throwable {
            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertNull(redirectUrl);
        }

        @Test
        void verifyOidcLogoutWithStateParam() throws Throwable {
            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OAuth20Constants.STATE, UUID.randomUUID().toString())
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertNull(redirectUrl);
        }

        @Test
        void verifyOidcLogoutIdTokenClientMismatch() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            val claims = getClaims(oidcRegisteredService.getClientId());
            servicesManager.save(oidcRegisteredService);

            val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString())
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyOidcLogoutWithIdTokenIssuerPerService() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            registeredService.setIdTokenIssuer("https://sso.example.org/cas/oidc/something/else");
            val claims = getClaims(registeredService.getClientId(), registeredService.getIdTokenIssuer());
            servicesManager.save(registeredService);

            val idToken = oidcTokenSigningAndEncryptionService.encode(registeredService, claims);
            mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT));
        }


        @Test
        void verifyOidcLogoutWithIdTokenParam() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            val claims = getClaims(oidcRegisteredService.getClientId());
            servicesManager.save(oidcRegisteredService);

            val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();

            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://oauth.example.org/logout?client_id=%s".formatted(oidcRegisteredService.getClientId()), redirectUrl);
        }

        @Test
        void verifyOidcLogoutWithIdTokenAndStateParams() throws Throwable {
            val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            val claims = getClaims(oidcRegisteredService.getClientId());
            servicesManager.save(oidcRegisteredService);
            val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();

            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=%s".formatted(oidcRegisteredService.getClientId()), redirectUrl);
        }

        @Test
        void verifyOidcLogoutWithIdTokenAndValidPostLogoutRedirectUrlParams() throws Throwable {

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            val claims = getClaims(registeredService.getClientId());
            servicesManager.save(registeredService);
            val idToken = oidcTokenSigningAndEncryptionService.encode(registeredService, claims);

            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OidcConstants.POST_LOGOUT_REDIRECT_URI, "https://logout")
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://logout?state=abcd1234&client_id=%s".formatted(registeredService.getClientId()), redirectUrl);
        }

        @Test
        void verifyOidcLogoutWithIdTokenAndInvalidPostLogoutRedirectUrlParams() throws Throwable {

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
            val claims = getClaims(registeredService.getClientId());
            servicesManager.save(registeredService);
            val idToken = oidcTokenSigningAndEncryptionService.encode(registeredService, claims);

            val request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OidcConstants.POST_LOGOUT_REDIRECT_URI, "https://invalidlogouturl")
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=%s".formatted(registeredService.getClientId()), redirectUrl);
        }
    }

    @Nested
    @Import(OidcLogoutMatcherTestConfiguration.class)
    class MatcherTests extends AbstractOidcTests {
        @Test
        void verifyOidcLogoutWithIdTokenAndValidRegExMatchingPostLogoutRedirectUrlParams() throws Throwable {
            val claims = getClaims();
            val oidcRegisteredService = getOidcRegisteredService(true, false);
            val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

            var request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OidcConstants.POST_LOGOUT_REDIRECT_URI, "https://www.acme.com/end")
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            var redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://www.acme.com/end?state=abcd1234&client_id=clientid", redirectUrl);

            request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OidcConstants.POST_LOGOUT_REDIRECT_URI, "https://www.acme.com/done")
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://www.acme.com/done?state=abcd1234&client_id=clientid", redirectUrl);

            request = mockMvc.perform(get("/cas/oidc/logout")
                    .with(withHttpRequestProcessor())
                    .queryParam(OidcConstants.ID_TOKEN_HINT, idToken)
                    .queryParam(OidcConstants.POST_LOGOUT_REDIRECT_URI, "https://www.acme.org/done")
                    .queryParam(OAuth20Constants.STATE, "abcd1234")
                )
                .andExpect(status().isOk())
                .andExpect(request().attribute("status", HttpStatus.PERMANENT_REDIRECT))
                .andReturn()
                .getRequest();
            redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
            assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=clientid", redirectUrl);
        }
    }

    @TestConfiguration(value = "OidcLogoutMatcherTestConfiguration", proxyBeanMethods = false)
    static class OidcLogoutMatcherTestConfiguration {
        @Bean(name = OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
        public OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher() {
            return String::matches;
        }
    }

}
