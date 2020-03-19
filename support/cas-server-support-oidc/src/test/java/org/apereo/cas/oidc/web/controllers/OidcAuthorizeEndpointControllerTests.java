package org.apereo.cas.oidc.web.controllers;

import com.nimbusds.openid.connect.sdk.OIDCResponseTypeValue;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.apereo.cas.oidc.OidcConstants.CLAIM_AT_HASH;
import static org.apereo.cas.oidc.OidcConstants.CLAIM_AUTH_TIME;
import static org.apereo.cas.oidc.OidcConstants.CLAIM_PREFERRED_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("OIDC")
@TestConfiguration
public class OidcAuthorizeEndpointControllerTests extends AbstractOidcTests {

    @Autowired
    protected OidcAuthorizeEndpointController oidcAuthorizeEndpointController;

    private static final String CLIENT_ID = "1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String SERVICE_NAME = "serviceName";

    private static final String FIRST_NAME_ATTRIBUTE = "firstName";

    private static final String LAST_NAME_ATTRIBUTE = "lastName";

    private static final String ID = "casuser";

    private static final String FIRST_NAME = "jerome";

    private static final String LAST_NAME = "LELEU";

    private MockHttpServletRequest mockRequest;

    private MockHttpServletResponse mockResponse;

    private HashMap<String, Object> attributes;

    private OAuthRegisteredService service;
    private MockTicketGrantingTicket tgt;

    @BeforeEach
    @Override
    public void initialize() {
        super.initialize();
        clearAllServices();

        tgt = new MockTicketGrantingTicket(ID);
        ticketRegistry.addTicket(tgt);

        mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val session = new MockHttpSession();
        mockRequest.setSession(session);
        Cookie tgtCookie = new Cookie(casProperties.getTgc().getName(), tgt.getId());
        mockRequest.setCookies(tgtCookie);

        mockResponse = new MockHttpServletResponse();

        service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        oidcAuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
                .set(new JEEContext(mockRequest, mockResponse, new JEESessionStore()), Pac4jConstants.USER_PROFILES, profile);
    }

    @Test
    public void verifyIdTokenRedirectToClient() throws Exception {
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OIDCResponseTypeValue.ID_TOKEN.getValue());

        val modelAndView = oidcAuthorizeEndpointController.handleRequest(mockRequest, mockResponse);

        val view = modelAndView.getView();
        assertThat(view).isInstanceOf(RedirectView.class);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();

        assertThat(redirectUrl).startsWith(REDIRECT_URI + "#access_token=");
        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (OAuth20AccessToken) this.ticketRegistry.getTicket(code);
        assertThat(accessToken).isNotNull();
        val principal = accessToken.getAuthentication().getPrincipal();
        assertThat(principal.getId()).isEqualTo(ID);
        val principalAttributes = principal.getAttributes();
        assertThat(principalAttributes).hasSize(attributes.size());
        assertThat(principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0)).isEqualTo(FIRST_NAME);

        val expiresIn = StringUtils.substringBetween(redirectUrl, "&expires_in=", "&id_token=");
        assertThat(expiresIn).isEqualTo(String.valueOf(casProperties.getTicket().getTgt().getTimeToKillInSeconds()));

        val idToken = StringUtils.substringAfter(redirectUrl, "&id_token=");
        JwtClaims claims = oidcTokenSigningAndEncryptionService.decode(idToken, Optional.of(service));
        assertThat(claims.getAudience()).isNotEmpty();
        assertThat(claims.getIssuer()).isEqualTo(casProperties.getAuthn().getOidc().getIssuer());
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpirationTime()).isNotNull();
        long idTokenExpiresIn = claims.getExpirationTime().getValue() - claims.getIssuedAt().getValue();
        assertThat(idTokenExpiresIn).isEqualTo(casProperties.getTicket().getTgt().getTimeToKillInSeconds());
        assertThat(claims.getSubject()).isEqualTo(ID);
        assertThat(claims.getClaimValueAsString(CLAIM_AT_HASH)).isNotBlank();
        assertThat(claims.getClaimValue(CLAIM_AUTH_TIME)).isInstanceOf(Long.class);
        assertThat(claims.getClaimValueAsString(OAuth20Constants.CLIENT_ID)).isEqualTo(CLIENT_ID);
        assertThat(claims.getJwtId()).isEqualTo(tgt.getId());
        assertThat(claims.getNotBefore()).isNotNull();
        assertThat(claims.hasClaim(OAuth20Constants.NONCE)).isTrue();
        assertThat(claims.getClaimValueAsString(CLAIM_PREFERRED_USERNAME)).isEqualTo(ID);
        assertThat(claims.hasClaim(OAuth20Constants.STATE)).isTrue();
    }

    private OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        val service = new OidcRegisteredService();
        service.setName(name);
        service.setServiceId(serviceId);
        service.setClientId(CLIENT_ID);
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of(FIRST_NAME_ATTRIBUTE)));
        return service;
    }

    private void clearAllServices() {
        val col = this.servicesManager.getAllServices();
        col.forEach(r -> this.servicesManager.delete(r.getId()));
    }
}
