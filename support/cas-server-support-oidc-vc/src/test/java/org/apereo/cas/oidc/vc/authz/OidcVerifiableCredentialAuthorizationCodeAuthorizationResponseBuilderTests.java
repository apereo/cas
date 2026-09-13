package org.apereo.cas.oidc.vc.authz;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.services.DefaultRegisteredServiceOidcVerifiableCredentialsPolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.JsonUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.given_name=CAS",
    "cas.authn.attribute-repository.stub.attributes.family_name=User",
    "cas.authn.attribute-repository.stub.attributes.email=casuser@example.org",
    "cas.authn.attribute-repository.stub.attributes.student_id=S12345",
    "cas.authn.attribute-repository.stub.attributes.active=true",
    "cas.authn.attribute-repository.stub.attributes.score=95.5",
    "cas.authn.attribute-repository.stub.attributes.roles=admin,user",

    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.mandatory=false",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.mandatory=false",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.mandatory=false",
    "cas.authn.oauth.session-replication.replicate-sessions=false",
    "cas.tgc.pin-to-session=false",
    "cas.tgc.crypto.enabled=false"
})
class OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilderTests extends AbstractOidcTests {
    @Test
    void verifyOperation() throws Exception {
        val registeredService = registerService(null);
        val codeChallenge = UUID.randomUUID().toString();
        val code = authorize(registeredService, codeChallenge);
        assertNotNull(code);

        exchangeForToken(registeredService, code, codeChallenge)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists())
            .andExpect(jsonPath("$." + OidcConstants.C_NONCE).doesNotExist())
            .andExpect(jsonPath("$." + OidcConstants.C_NONCE_EXPIRES_IN).doesNotExist())
            .andExpect(jsonPath("$." + OAuth20Constants.AUTHORIZATION_DETAILS).exists())
            .andExpect(jsonPath("$." + OAuth20Constants.AUTHORIZATION_DETAILS + "[0].credential_identifiers").isArray());
    }

    /**
     * Authorization details are a request, not a grant. A credential configuration this service's
     * policy does not permit never reaches the code, so the token carries no authorization for it and
     * the credential endpoint has nothing to honour.
     */
    @Test
    void verifyAuthorizationDetailsAreDroppedWhenThePolicyDisallowsTheCredential() throws Exception {
        val registeredService = registerService(Set.of("SomeOtherCredential"));
        val codeChallenge = UUID.randomUUID().toString();
        val code = authorize(registeredService, codeChallenge);
        assertNotNull(code);

        exchangeForToken(registeredService, code, codeChallenge)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists())
            .andExpect(jsonPath("$." + OAuth20Constants.AUTHORIZATION_DETAILS).doesNotExist());
    }

    @Test
    void verifyPolicyWithoutCredentialTypesAllowsTheCredential() throws Exception {
        val registeredService = registerService(Set.of());
        val codeChallenge = UUID.randomUUID().toString();
        val code = authorize(registeredService, codeChallenge);
        assertNotNull(code);

        exchangeForToken(registeredService, code, codeChallenge)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + OAuth20Constants.AUTHORIZATION_DETAILS).exists());
    }

    private OidcRegisteredService registerService(@Nullable final Set<String> allowedCredentialTypes) {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.getClientSecrets().clear();
        registeredService.setBypassApprovalPrompt(true);
        if (allowedCredentialTypes != null) {
            registeredService.setVerifiableCredentialsPolicy(
                new DefaultRegisteredServiceOidcVerifiableCredentialsPolicy(allowedCredentialTypes));
        }
        servicesManager.save(registeredService);
        return registeredService;
    }

    private String authorize(final OidcRegisteredService registeredService, final String codeChallenge) throws Exception {
        val profile = new CasProfile();
        profile.setId("casuser");
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT);
        val ticketGrantingTicket = new MockTicketGrantingTicket(profile.getId());
        ticketRegistry.addTicket(ticketGrantingTicket);
        profile.addAttribute(TicketGrantingTicket.class.getName(), ticketGrantingTicket.getId());

        val redirectedUrl = mockMvc.perform(get("/cas/oidc/" + OidcConstants.AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.ISSUER_STATE, UUID.randomUUID().toString())
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
                .param(OAuth20Constants.CODE_CHALLENGE, codeChallenge)
                .param(OAuth20Constants.CODE_CHALLENGE_METHOD, "plain")
                .param(OAuth20Constants.AUTHORIZATION_DETAILS,
                    JsonUtils.render(List.of(OidcVerifiableCredentialAuthorizationDetails.builder().credentialConfigurationId("myorg").build())))
                .with(withHttpRequestProcessor())
                .with(request -> {
                    request.addHeader(HttpHeaders.USER_AGENT, "test");
                    val response = new MockHttpServletResponse();
                    ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicket.getId());
                    request.setCookies(response.getCookies());
                    val context = new JEEContext(request, response);

                    val manager = oauthSecProfileManagerFactory.apply(context, oauthDistributedSessionStore);
                    manager.save(true, profile, false);
                    return request;
                })
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("https://oauth.example.org*"))
            .andReturn()
            .getResponse()
            .getRedirectedUrl();
        assertNotNull(redirectedUrl);
        return UriComponentsBuilder.fromUri(URI.create(redirectedUrl))
            .build()
            .getQueryParams()
            .getFirst(OAuth20Constants.CODE);
    }

    private ResultActions exchangeForToken(final OidcRegisteredService registeredService,
                                           final String code, final String codeVerifier) throws Exception {
        return mockMvc.perform(post("/cas/oidc/" + OidcConstants.TOKEN_URL)
            .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .param(OAuth20Constants.CODE, code)
            .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org")
            .param(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name())
            .param(OAuth20Constants.CODE_VERIFIER, codeVerifier)
            .with(withHttpRequestProcessor()));
    }
}
