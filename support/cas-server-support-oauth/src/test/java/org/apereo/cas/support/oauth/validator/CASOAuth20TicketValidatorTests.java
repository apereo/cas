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
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
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
        val profile = validateServiceTicketFor("casuser", RegisteredServiceTestUtils.getTestAttributes());
        assertEquals("casuser", profile.getId());
        assertEquals(OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME, profile.getAuthenticationHandlerName());

        val attributes = profile.getAttributes();
        assertTrue(attributes.containsKey(TicketGrantingTicket.class.getName()));
        assertTrue(attributes.containsKey(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY));
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("memberOf"));

        val authenticationAttributes = profile.getAuthenticationAttributes();
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(authenticationAttributes.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
        assertTrue(authenticationAttributes.containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    @Test
    void verifyTypedIdentifierAndAttributeValues() throws Throwable {
        val attributes = new HashMap<String, Object>(RegisteredServiceTestUtils.getTestAttributes());
        attributes.put("email", List.of("casuser@example.org", "cas@example.org"));
        val profile = validateServiceTicketFor("org.pac4j.oauth.profile.github.GitHubProfile#12345", attributes);
        assertEquals("12345", profile.getId());
        assertEquals(List.of("casuser@example.org", "cas@example.org"), List.copyOf((Collection<?>) profile.getAttribute("email")));
        assertEquals("example#12345", validateServiceTicketFor("example#12345", attributes).getId());
    }

    @Test
    void verifyUnknownTicket() {
        val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()) + "?client_name=" + Authenticators.CAS_OAUTH_CLIENT;
        val validator = new CASOAuth20TicketValidator(new InternalTicketValidator(centralAuthenticationService,
            serviceFactory, authenticationAttributeReleasePolicy, servicesManager), authenticationAttributeReleasePolicy);
        assertThrows(Throwable.class, () -> validator.validate("ST-" + UUID.randomUUID(), callbackUrl));
    }

    private OAuth20UserProfile validateServiceTicketFor(final String principalId, final Map<String, ?> attributes) throws Throwable {
        val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()) + "?client_name=" + Authenticators.CAS_OAUTH_CLIENT;
        val tgt = new MockTicketGrantingTicket(principalId, attributes);
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
        val validatedTicket = validator.validate(st.getId(), st.getService().getId());
        assertFalse(validatedTicket.fromNewLogin());
        return validatedTicket.profile();
    }
}
