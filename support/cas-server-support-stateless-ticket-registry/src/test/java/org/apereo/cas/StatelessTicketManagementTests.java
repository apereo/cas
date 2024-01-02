package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.config.StatelessTicketRegistryConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StatelessTicketManagementTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("CAS")
@Import(StatelessTicketRegistryConfiguration.class)
public class StatelessTicketManagementTests extends AbstractCentralAuthenticationServiceTests {
    @Test
    void verifyGrantServiceTicketAndValidate() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("eduPersonTest");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        assertTrue(ticketGrantingTicket.isStateless());
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);
        assertTrue(serviceTicket.isStateless());

        val validatedAssertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        assertNotNull(validatedAssertion);
        assertTrue(validatedAssertion.isStateless());
        assertEquals(1, validatedAssertion.getChainedAuthentications().size());
        assertTrue(validatedAssertion.getContext().isEmpty());

        val authentication = validatedAssertion.getPrimaryAuthentication();
        assertTrue(authentication.getSuccesses().containsKey(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(authentication.getAttributes().containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        assertEquals("developer", authentication.getPrincipal().getId());
        val attributes = authentication.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("groupMembership"));
        assertTrue(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("eduPersonAffiliation"));
        assertTrue(attributes.containsKey("binaryAttribute"));
    }
}
