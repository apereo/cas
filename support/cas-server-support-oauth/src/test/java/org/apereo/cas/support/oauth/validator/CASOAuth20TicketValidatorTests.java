package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CASOAuth20TicketValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OAuth")
class CASOAuth20TicketValidatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    protected TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
    private AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Test
    void verifyOperation() throws Throwable {
        val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()) + "?client_name=" + oauthCasClient.getName();
        val tgt = new MockTicketGrantingTicket("casuser", RegisteredServiceTestUtils.getTestAttributes());
        ticketRegistry.addTicket(tgt);
        val st = tgt.grantServiceTicket(RegisteredServiceTestUtils.getService(callbackUrl), serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(st);
        ticketRegistry.updateTicket(tgt);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(callbackUrl, Map.of());
        val releasePolicy = new ReturnAllAttributeReleasePolicy();
        releasePolicy.setAuthorizedToReleaseAuthenticationAttributes(true);
        registeredService.setAttributeReleasePolicy(releasePolicy);
        val registeredServices = mock(ServicesManager.class);
        when(registeredServices.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val validator = new CASOAuth20TicketValidator(new InternalTicketValidator(centralAuthenticationService,
            serviceFactory, authenticationAttributeReleasePolicy, registeredServices), authenticationAttributeReleasePolicy);
        val assertion = validator.validate(st.getId(), st.getService().getId());

        val attributes = assertion.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey(TicketGrantingTicket.class.getName()));
        assertTrue(attributes.containsKey(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY));
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("memberOf"));

        val authenticationAttributes = assertion.getAttributes();
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(authenticationAttributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }
}
