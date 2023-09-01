package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CASOAuth20TicketValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OAuth")
class CASOAuth20TicketValidatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(ServiceTicketSessionTrackingPolicy.BEAN_NAME)
    protected ServiceTicketSessionTrackingPolicy serviceTicketSessionTrackingPolicy;

    @BeforeEach
    @Override
    public void setup() {
        super.setup();
        clearAllServices();
    }

    @Test
    void verifyOperation() throws Throwable {
        val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix())
            + "?client_name=" + oauthCasClient.getName();

        val tgt = new MockTicketGrantingTicket("casuser", RegisteredServiceTestUtils.getTestAttributes());
        ticketRegistry.addTicket(tgt);
        val st = tgt.grantServiceTicket(RegisteredServiceTestUtils.getService(callbackUrl), serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(st);
        ticketRegistry.updateTicket(tgt);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(callbackUrl, Map.of());
        registeredService.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        registeredService.setMatchingStrategy(new LiteralRegisteredServiceMatchingStrategy());
        servicesManager.save(registeredService);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(CasProtocolConstants.PARAMETER_TICKET, st.getId());
        mockRequest.setParameter(CasProtocolConstants.PARAMETER_SERVICE, st.getService().getId());
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        val mockResponse = new MockHttpServletResponse();
        val callContext = new CallContext(new JEEContext(mockRequest, mockResponse),
            oauthDistributedSessionStore, ProfileManagerFactory.DEFAULT);
        val credentials = oauthCasClient.getCredentials(callContext);
        assertTrue(credentials.isPresent());
        val validated = oauthCasClient.validateCredentials(callContext, credentials.orElseThrow());
        assertTrue(validated.isPresent());
        val profileResult = oauthCasClient.getUserProfile(callContext, validated.orElseThrow());
        assertTrue(profileResult.isPresent());
        val up = (BasicUserProfile) profileResult.get();
        assertTrue(up.containsAttribute(TicketGrantingTicket.class.getName()));
        assertTrue(up.containsAttribute("uid"));
        assertTrue(up.containsAttribute("givenName"));
        assertTrue(up.containsAttribute("memberOf"));

        assertTrue(up.containsAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(up.containsAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(up.containsAuthenticationAttribute(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(up.containsAuthenticationAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }
}
