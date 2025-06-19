package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcScopeFreeAttributeReleasePolicy;
import org.apereo.cas.oidc.services.DefaultRegisteredServiceOidcIdTokenExpirationPolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenHashGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.apereo.cas.oidc.OidcConstants.StandardScopes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcIdTokenGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
class OidcIdTokenGeneratorServiceTests {

    private static final String OIDC_CLAIM_EMAIL = "email";

    private static final String OIDC_CLAIM_PHONE_NUMBER = "phone_number";

    private static final String OIDC_CLAIM_NAME = "name";

    private static final String OIDC_CLAIM_PREFERRED_USERNAME = "preferred_username";


    private OAuth20AccessToken buildAccessToken(final TicketGrantingTicket tgt, final Set<String> scope) {
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());
        when(accessToken.getClientId()).thenReturn("client");
        when(accessToken.getScopes()).thenReturn(scope);
        return accessToken;
    }

    protected OAuth20AccessToken buildAccessToken(final MockTicketGrantingTicket tgt) {
        return buildAccessToken(tgt, Set.of(OPENID.getScope(), PROFILE.getScope(), EMAIL.getScope(), PHONE.getScope()));
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/oidc-definitions.json",
        "cas.authn.oidc.discovery.claims=sub,name,organization",
        "cas.authn.oauth.access-token.crypto.encryption-enabled=false",
        "cas.authn.oidc.id-token.include-id-token-claims=true"
    })
    class AttributeDefinitionStoreTests extends AbstractOidcTests {
        @Test
        void verifyClaimReleaseAllowedByScopeFreePolicyInChain() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"), "sys_id", List.of("1234567890")));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(
                    OAuth20Constants.STATE, List.of(UUID.randomUUID().toString()),
                    OAuth20Constants.NONCE, List.of(UUID.randomUUID().toString())));
            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setIdTokenIssuer(UUID.randomUUID().toString());
            registeredService.setScopes(CollectionUtils.wrapSet(OPENID.getScope(), PROFILE.getScope()));
            registeredService.setAttributeReleasePolicy(new ChainingAttributeReleasePolicy().addPolicies(new OidcScopeFreeAttributeReleasePolicy(List.of("sys_id"))));
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();

            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertEquals("1234567890", claims.getClaimValueAsString("sys_id"));
        }
        
        @Test
        void verifyClaimReleaseAllowedByScopeFreePolicy() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"), "sys_id", List.of("1234567890")));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(
                    OAuth20Constants.STATE, List.of(UUID.randomUUID().toString()),
                    OAuth20Constants.NONCE, List.of(UUID.randomUUID().toString())));
            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setIdTokenIssuer(UUID.randomUUID().toString());
            registeredService.setScopes(CollectionUtils.wrapSet(OPENID.getScope()));
            registeredService.setAttributeReleasePolicy(new OidcScopeFreeAttributeReleasePolicy(List.of("sys_id")));
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();

            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertEquals("1234567890", claims.getClaimValueAsString("sys_id"));
        }
        
        @Test
        void verifyOperation() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"), "org.apereo.cas.entity", List.of("example", "apereo")));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(
                    OAuth20Constants.STATE, List.of(UUID.randomUUID().toString()),
                    OAuth20Constants.NONCE, List.of(UUID.randomUUID().toString())));
            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setIdTokenIssuer(UUID.randomUUID().toString());
            registeredService.setScopes(CollectionUtils.wrapSet(OPENID.getScope()));
            registeredService.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("org.apereo.cas.entity")));
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertEquals("{apereo={cas={entity=[example, apereo]}}}", claims.getClaimValueAsString("org"));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oauth.access-token.crypto.encryption-enabled=false",
        "cas.authn.oidc.id-token.include-id-token-claims=true"
    })
    class IncludeIDTokenClaimsTests extends AbstractOidcTests {
        @Test
        void verifyTokenGenerationWithClaimsForCodeResponseType() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val phoneValues = List.of("123456789", "4805553241");
            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                "color", List.of("yellow"),
                OIDC_CLAIM_PHONE_NUMBER, phoneValues,
                OIDC_CLAIM_NAME, List.of(profile.getId())));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));

            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setScopes(CollectionUtils.wrapSet(EMAIL.getScope(), PROFILE.getScope(), PHONE.getScope()));


            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.CODE)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertTrue(claims.hasClaim(OIDC_CLAIM_EMAIL));
            assertTrue(claims.hasClaim(OIDC_CLAIM_NAME));
            assertTrue(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.id-token.include-id-token-claims=false",
        "cas.authn.oauth.access-token.crypto.encryption-enabled=false",
        "cas.authn.oidc.core.claims-map.preferred_username=custom-attribute"
    })
    class DefaultTests extends AbstractOidcTests {

        @Test
        void verifyNoIdTokenForMissingOpenIdScope() throws Throwable {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
            val tgt = new MockTicketGrantingTicket(authentication);
            val accessToken = buildAccessToken(tgt, Set.of(EMAIL.getScope()));

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            servicesManager.save(registeredService);
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(authentication.getPrincipal().getId());


            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNull(idToken);
        }

        @Test
        void verifyTokenGeneration() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val phoneValues = List.of("\\\\123456789", "4805553241");
            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                OIDC_CLAIM_PHONE_NUMBER, phoneValues,
                "color", List.of("yellow"),
                "custom-attribute", "test",
                OIDC_CLAIM_NAME, List.of(profile.getId())));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));
            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));
            ticketRegistry.addTicket(tgt);
            
            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setIdTokenIssuer(UUID.randomUUID().toString());
            registeredService.setScopes(CollectionUtils.wrapSet(EMAIL.getScope(), PROFILE.getScope(), PHONE.getScope()));
            servicesManager.save(registeredService);


            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertTrue(claims.hasClaim(OIDC_CLAIM_EMAIL));
            assertEquals(authentication.getAuthenticationDate().toEpochSecond(), (long) claims.getClaimValue(OidcConstants.CLAIM_AUTH_TIME));
            assertTrue(claims.hasClaim(OIDC_CLAIM_NAME));
            assertTrue(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
            assertTrue(claims.hasClaim(OIDC_CLAIM_PREFERRED_USERNAME));
            assertEquals("casuser@example.org", claims.getStringClaimValue(OIDC_CLAIM_EMAIL));
            assertEquals(profile.getId(), claims.getStringClaimValue(OIDC_CLAIM_NAME));
            assertEquals(phoneValues, claims.getStringListClaimValue(OIDC_CLAIM_PHONE_NUMBER));
            assertEquals("test", claims.getStringClaimValue(OIDC_CLAIM_PREFERRED_USERNAME));
            assertEquals(registeredService.getIdTokenIssuer(), claims.getStringClaimValue(OidcConstants.ISS));
        }

        @Test
        void verifyTokenGenerationWithoutClaimsForCodeResponseType() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val phoneValues = List.of("123456789", "4805553241");
            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                OIDC_CLAIM_PHONE_NUMBER, phoneValues,
                "color", List.of("yellow"),
                OIDC_CLAIM_NAME, List.of(profile.getId())));

            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));
            val tgt = new MockTicketGrantingTicket(authentication);
            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));
            ticketRegistry.addTicket(tgt);

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setIdTokenExpirationPolicy(new DefaultRegisteredServiceOidcIdTokenExpirationPolicy("PT60S"));
            registeredService.setScopes(CollectionUtils.wrapSet(EMAIL.getScope(), PROFILE.getScope(), PHONE.getScope()));
            servicesManager.save(registeredService);


            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.CODE)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);

            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertFalse(claims.hasClaim(OIDC_CLAIM_EMAIL));
            assertFalse(claims.hasClaim(OIDC_CLAIM_NAME));
            assertFalse(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
        }

        @Test
        void verifyTokenGenerationWithOutClaimsForAuthzCodeGrantType() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val phoneValues = List.of("123456789", "4805553241");
            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                "color", List.of("yellow"),
                OIDC_CLAIM_PHONE_NUMBER, phoneValues,
                OIDC_CLAIM_NAME, List.of(profile.getId())));

            var authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));
            val tgt = new MockTicketGrantingTicket(authentication);

            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setScopes(CollectionUtils.wrapSet(EMAIL.getScope(), PROFILE.getScope(), PHONE.getScope()));
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);
            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertFalse(claims.hasClaim(OIDC_CLAIM_EMAIL));
            assertFalse(claims.hasClaim(OIDC_CLAIM_NAME));
            assertFalse(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
        }

        @Test
        void verifyTokenGenerationWithOutClaimsForRefreshTokenGrantType() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());

            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val phoneValues = List.of("123456789", "4805553241");
            val principal = RegisteredServiceTestUtils.getPrincipal(profile.getId(), CollectionUtils.wrap(
                OIDC_CLAIM_EMAIL, List.of("casuser@example.org"),
                "color", List.of("yellow"),
                OIDC_CLAIM_PHONE_NUMBER, phoneValues,
                OIDC_CLAIM_NAME, List.of(profile.getId())));

            var authentication = CoreAuthenticationTestUtils.getAuthentication(principal,
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce")));
            val tgt = new MockTicketGrantingTicket(authentication);

            val callback = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/'
                + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

            val service = webApplicationServiceFactory.createService(callback);
            tgt.getServices().putAll(CollectionUtils.wrap("service", service));

            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            registeredService.setScopes(CollectionUtils.wrapSet(EMAIL.getScope(), PROFILE.getScope(), PHONE.getScope()));
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.ID_TOKEN)
                .grantType(OAuth20GrantTypes.REFRESH_TOKEN)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);
            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertFalse(claims.hasClaim(OIDC_CLAIM_EMAIL));
            assertFalse(claims.hasClaim(OIDC_CLAIM_NAME));
            assertFalse(claims.hasClaim(OIDC_CLAIM_PHONE_NUMBER));
        }

        @Test
        void verifyTokenGenerationWithoutCallbackService() throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());
            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val mfa = casProperties.getAuthn().getMfa();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(profile.getId(),
                CollectionUtils.wrap(OAuth20Constants.STATE, List.of("some-state"),
                    OAuth20Constants.NONCE, List.of("some-nonce"),
                    mfa.getCore().getAuthenticationContextAttribute(), List.of("context-class"),
                    AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, List.of("Handler1")));

            val tgt = new MockTicketGrantingTicket(authentication);
            ticketRegistry.addTicket(tgt);
            
            val accessToken = buildAccessToken(tgt);

            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl());
            servicesManager.save(registeredService);

            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.CODE)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);
        }

        @RepeatedTest(2)
        void verifyAccessTokenAsJwt(final RepetitionInfo repetitionInfo) throws Throwable {
            val request = new MockHttpServletRequest();
            val profile = new CommonProfile();
            profile.setClientName("OIDC");
            profile.setId(UUID.randomUUID().toString());
            request.setAttribute(Pac4jConstants.USER_PROFILES,
                CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

            val accessToken = getAccessToken();
            val registeredService = getOidcRegisteredService(accessToken.getClientId());
            registeredService.setJwtAccessToken(true);
            registeredService.setIdTokenSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);

            registeredService.setProperties(Map.of(
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(),
                new DefaultRegisteredServiceProperty("false"),
                RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_ENABLED.getPropertyName(),
                new DefaultRegisteredServiceProperty(repetitionInfo.getCurrentRepetition() % 2 == 0 ? "false" : "true")
            ));

            servicesManager.save(registeredService);
            val idTokenContext = IdTokenGenerationContext.builder()
                .accessToken(accessToken)
                .userProfile(profile)
                .responseType(OAuth20ResponseTypes.CODE)
                .grantType(OAuth20GrantTypes.NONE)
                .registeredService(registeredService)
                .build();
            val idToken = oidcIdTokenGenerator.generate(idTokenContext);
            assertNotNull(idToken);
            val claims = oidcTokenSigningAndEncryptionService.decode(idToken.token(), Optional.of(registeredService));
            assertNotNull(claims);
            assertTrue(claims.hasClaim(OidcConstants.CLAIM_AT_HASH));
            assertTrue(claims.hasClaim(OidcConstants.CLAIM_AUTH_TIME));
            val issuer = oidcIssuerService.determineIssuer(Optional.of(registeredService));
            assertEquals(issuer, claims.getIssuer());

            val hash = claims.getClaimValue(OidcConstants.CLAIM_AT_HASH, String.class);
            val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(oidcConfigurationContext,
                registeredService, accessToken, issuer);
            val encodedAccessToken = cipher.encode(accessToken.getId());
            val newHash = OAuth20TokenHashGenerator.builder()
                .token(encodedAccessToken)
                .registeredService(registeredService)
                .algorithm(registeredService.getIdTokenSigningAlg())
                .build()
                .generate();
            assertEquals(hash, newHash);
        }
    }
}
