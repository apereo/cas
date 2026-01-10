package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasOidcAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("AuthenticationHandler")
@ImportAutoConfiguration({
    CasOAuth20AutoConfiguration.class,
    CasOidcAutoConfiguration.class
})
@TestPropertySource(properties = "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/tokenauthn.jwks")
class OidcTokenAuthenticationHandlerTests extends BaseTokenAuthenticationTests {
    @Autowired
    @Qualifier("oidcTokenAuthenticationHandler")
    private AuthenticationHandler oidcTokenAuthenticationHandler;

    @Autowired
    @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Autowired
    @Qualifier("oidcIdTokenGenerator")
    private IdTokenGeneratorService oidcIdTokenGenerator;
    
    @Test
    void verifyFailsOperation() throws Throwable {
        val serviceId = "https://token.example.org/%s".formatted(UUID.randomUUID().toString());
        val service = RegisteredServiceTestUtils.getService(serviceId);
        val credential = new TokenCredential(UUID.randomUUID().toString(), service);
        assertTrue(oidcTokenAuthenticationHandler.supports(credential));
        assertThrows(AuthenticationException.class, () -> oidcTokenAuthenticationHandler.authenticate(credential, service));
    }
    
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(oidcTokenAuthenticationHandler);
        val serviceId = "https://token.example.org/%s".formatted(UUID.randomUUID().toString());
        val service = RegisteredServiceTestUtils.getService(serviceId);

        val clientId = UUID.randomUUID().toString();
        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser",
            CollectionUtils.wrap(
                "email", List.of("casuser@example.org"),
                "family_name", List.of("apereo")));
        val accessToken = OAuth20TestUtils.getAccessToken(ticketGrantingTicket,
            UUID.randomUUID().toString(), serviceId, clientId);
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        ticketRegistry.addTicket(accessToken);

        val registeredService = new OidcRegisteredService();
        registeredService.setClientId(accessToken.getClientId());
        registeredService.setName("oidc-%s".formatted(UUID.randomUUID().toString()));
        registeredService.setClientSecret("secret");
        registeredService.setJwtAccessToken(true);
        registeredService.setServiceId(serviceId);
        registeredService.setScopes(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()
        ));
        servicesManager.save(registeredService);

        val encoder = OAuth20JwtAccessTokenEncoder.toEncodableCipher(oauth20ConfigurationContext, registeredService, accessToken);
        val encodedAccessToken = encoder.encode(accessToken.getId());

        val credential = new TokenCredential(encodedAccessToken, service);
        assertTrue(oidcTokenAuthenticationHandler.supports(credential));

        val result = oidcTokenAuthenticationHandler.authenticate(credential, service);
        assertEquals(result.getPrincipal(), ticketGrantingTicket.getAuthentication().getPrincipal());
    }

    @Test
    void verifyIdTokenAuthentication() throws Throwable {
        val serviceId = "https://idtoken.example.org/%s".formatted(UUID.randomUUID().toString());
        val service = RegisteredServiceTestUtils.getService(serviceId);

        val clientId = UUID.randomUUID().toString();
        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser",
            CollectionUtils.wrap(
                "email", List.of("casuser@example.org"),
                "family_name", List.of("apereo")));
        val accessToken = OAuth20TestUtils.getAccessToken(ticketGrantingTicket,
            UUID.randomUUID().toString(), serviceId, clientId);
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        ticketRegistry.addTicket(accessToken);

        val registeredService = new OidcRegisteredService();
        registeredService.setClientId(accessToken.getClientId());
        registeredService.setName("oidc-%s".formatted(UUID.randomUUID().toString()));
        registeredService.setClientSecret("secret");
        registeredService.setJwtAccessToken(true);
        registeredService.setServiceId(serviceId);
        registeredService.setScopes(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope()
        ));
        servicesManager.save(registeredService);

        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId(accessToken.getAuthentication().getPrincipal().getId());
        
        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(profile)
            .responseType(OAuth20ResponseTypes.ID_TOKEN)
            .grantType(OAuth20GrantTypes.NONE)
            .registeredService(registeredService)
            .build();
        val idToken = oidcIdTokenGenerator.generate(idTokenContext);
        assertNotNull(idToken);

        val credential = new TokenCredential(idToken.token(), service);
        assertTrue(oidcTokenAuthenticationHandler.supports(credential));

        val result = oidcTokenAuthenticationHandler.authenticate(credential, service);
        assertEquals(result.getPrincipal(), ticketGrantingTicket.getAuthentication().getPrincipal());
    }
}
