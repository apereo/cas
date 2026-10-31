package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20DefaultCasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "cas.authn.oauth.session-replication.replicate-sessions=false")
class OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests {
    private static final String SUPPORTING_CLIENT_ID = UUID.randomUUID().toString();

    private static final String NON_SUPPORTING_CLIENT_ID = UUID.randomUUID().toString();

    private static final String PROMISCUOUS_CLIENT_ID = UUID.randomUUID().toString();

    @Nested
    @TestPropertySource(properties = "cas.authn.oauth.code.remove-related-access-tokens=true")
    class RemovingInvalidTokenTests extends AbstractOAuth20Tests {
        @Autowired
        @Qualifier("oauthAuthorizationCodeGrantTypeTokenRequestValidator")
        private OAuth20TokenRequestValidator validator;

        @Test
        void verifyPreviousAccessTokensRemoved() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);

            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser");
            val registeredService = addRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL3, UUID.randomUUID().toString());
            val code = addCode(principal, registeredService);
            val at1 = addAccessToken(principal, registeredService, code);
            assertNotNull(at1);
            val at2 = addAccessToken(principal, registeredService, code);
            assertNotNull(at2);
            val recordProperties = new HashMap<String, Serializable>();
            recordProperties.put(OAuth20Utils.REDEEMED_CODE_TOKENS, new ArrayList<>(List.of(at1.getId(), at2.getId())));
            val recordId = OAuth20Utils.getRedeemedCodeTicketId(code.getId());
            ticketRegistry.addTicket(new TransientSessionTicketImpl(recordId, NeverExpiresExpirationPolicy.INSTANCE,
                code.getService(), recordProperties));
            ticketRegistry.deleteTicket(code);

            request.setParameter(OAuth20Constants.CODE, code.getId());
            profile.setId(registeredService.getClientId());
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, registeredService.getServiceId());
            storeProfileIntoSession(request, profile);
            assertFalse(validator.validate(request, response));
            assertNull(ticketRegistry.getTicket(at1.getId()));
            assertNull(ticketRegistry.getTicket(at2.getId()));
            assertNull(ticketRegistry.getTicket(recordId));
        }
    }

    @Nested
    class DefaultTests extends AbstractOAuth20Tests {
        private OAuthRegisteredService supportingService;

        private OAuth20Code supportingServiceTicket;

        private OAuth20Code nonSupportingServiceTicket;

        private OAuth20Code promiscuousServiceTicket;

        @Autowired
        @Qualifier("oauthAuthorizationCodeGrantTypeTokenRequestValidator")
        private OAuth20TokenRequestValidator validator;

        @BeforeEach
        void before() throws Throwable {
            supportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL,
                SUPPORTING_CLIENT_ID,
                SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
            val nonSupportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL2,
                NON_SUPPORTING_CLIENT_ID,
                NON_SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
            val promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
                RegisteredServiceTestUtils.CONST_TEST_URL3,
                PROMISCUOUS_CLIENT_ID,
                PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET);

            this.supportingServiceTicket = registerTicket(supportingService);
            this.nonSupportingServiceTicket = registerTicket(nonSupportingService);
            this.promiscuousServiceTicket = registerTicket(promiscuousService);

            this.servicesManager.save(supportingService, nonSupportingService, promiscuousService);
        }

        @Test
        void verifyClientSecretInRequest() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.GET.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);

            val secret = UUID.randomUUID().toString();
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.CLIENT_SECRET, secret);
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);
            request.setParameter(OAuth20Constants.CODE, secret);
            request.setQueryString(OAuth20Constants.CLIENT_SECRET + '=' + secret);
            assertFalse(validator.validate(request, response));
        }

        @Test
        void verifyBadToken() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);

            request.setParameter(OAuth20Constants.CODE, "UnknownToken");
            assertFalse(validator.validate(request, response));
        }

        @Test
        void verifyBadService() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);

            request.setParameter(OAuth20Constants.CODE, nonSupportingServiceTicket.getId());
            assertFalse(validator.validate(request, response));
        }

        @Test
        void verifyBadRequest() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);
            assertFalse(validator.validate(request, response));
        }

        @Test
        void verifyUnknownCodeRevokesPreviousAccessTokens() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);

            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser");
            val at = addAccessToken(principal, addRegisteredService());

            val code = ticketRegistry.getTicket(at.getToken(), OAuth20Code.class);
            assertNotNull(code);
            code.markTicketExpired();

            request.setParameter(OAuth20Constants.CODE, code.getId());
            profile.setId(PROMISCUOUS_CLIENT_ID);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL3);
            storeProfileIntoSession(request, profile);
            assertFalse(validator.validate(request, response));
            assertNull(ticketRegistry.getTicket(at.getToken()));
        }

        @Test
        void verifyOperation() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);

            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);

            request.setParameter(OAuth20Constants.CODE, supportingServiceTicket.getId());
            assertTrue(validator.validate(request, response));

            request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
            assertFalse(validator.validate(request, response));

            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
            assertFalse(validator.validate(request, response));
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

            request.setParameter(OAuth20Constants.CODE, nonSupportingServiceTicket.getId());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL2);
            profile.setId(NON_SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            assertFalse(validator.validate(request, response));

            request.setParameter(OAuth20Constants.CODE, promiscuousServiceTicket.getId());
            profile.setId(PROMISCUOUS_CLIENT_ID);
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL3);
            storeProfileIntoSession(request, profile);
            assertTrue(validator.validate(request, response));
        }

        @Test
        void verifyCodeChallengeEnforcedForAnyClientAuthentication() throws Throwable {
            val codeVerifier = RandomUtils.randomAlphanumeric(43);
            val codeChallenge = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256(codeVerifier));
            val code = registerTicket(supportingService, codeChallenge, "S256");

            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);
            request.setParameter(OAuth20Constants.CODE, code.getId());
            assertFalse(validator.validate(request, response));

            request.setParameter(OAuth20Constants.CODE_VERIFIER, RandomUtils.randomAlphanumeric(43));
            assertFalse(validator.validate(request, response));

            request.setParameter(OAuth20Constants.CODE_VERIFIER, codeVerifier);
            assertTrue(validator.validate(request, response));
        }

        @Test
        void verifyCodeChallengeComparedExactly() throws Throwable {
            val codeVerifier = "AbCdEfGhIjKlMnOpQrStUvWxYz0123456789-._~AbC";
            val code = registerTicket(supportingService, codeVerifier, "plain");

            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);
            request.setParameter(OAuth20Constants.CODE, code.getId());
            request.setParameter(OAuth20Constants.CODE_VERIFIER, codeVerifier.toLowerCase(Locale.ENGLISH));
            assertFalse(validator.validate(request, response));
        }

        @Test
        void verifyCodeVerifierWithoutCodeChallenge() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());
            val response = new MockHttpServletResponse();
            val profile = new DefaultOAuth20UserProfile(SUPPORTING_CLIENT_ID);
            storeProfileIntoSession(request, profile);
            request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
            request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);
            request.setParameter(OAuth20Constants.CODE, supportingServiceTicket.getId());
            request.setParameter(OAuth20Constants.CODE_VERIFIER, RandomUtils.randomAlphanumeric(43));
            assertFalse(validator.validate(request, response));
        }

        private OAuth20Code registerTicket(final OAuthRegisteredService service) throws Throwable {
            return registerTicket(service, null, null);
        }

        private OAuth20Code registerTicket(final OAuthRegisteredService service, @Nullable final String codeChallenge,
                                           @Nullable final String codeChallengeMethod)
                throws Throwable {
            val builder = new OAuth20DefaultCasAuthenticationBuilder(
                PrincipalFactoryUtils.newPrincipalFactory(),
                serviceFactory,
                new DefaultOAuth20ProfileScopeToAttributesFilter(),
                oauthRequestParameterResolver,
                casProperties);
            val oauthCasAuthenticationBuilderService = builder.buildService(service, null, false);
            val expirationPolicy = new ExpirationPolicyBuilder() {
                @Serial
                private static final long serialVersionUID = 3911344031977989503L;

                @Override
                public ExpirationPolicy buildTicketExpirationPolicy() {
                    return new OAuth20CodeExpirationPolicy(1, 60);
                }
            };

            val oauthCode = new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(), expirationPolicy,
                mock(ServicesManager.class), CipherExecutor.noOpOfStringToString(), TicketTrackingPolicy.noOp())
                .create(oauthCasAuthenticationBuilderService, RegisteredServiceTestUtils.getAuthentication(),
                    new MockTicketGrantingTicket("casuser"), new HashSet<>(),
                    codeChallenge, codeChallengeMethod, "clientid12345",
                    new HashMap<>(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
            this.ticketRegistry.addTicket(oauthCode);
            return oauthCode;
        }
    }
}
