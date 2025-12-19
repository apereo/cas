package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasStatelessTicketRegistryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link StatelessTicketManagementTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("CAS")
@ImportAutoConfiguration(CasStatelessTicketRegistryAutoConfiguration.class)
class StatelessTicketManagementTests extends AbstractCentralAuthenticationServiceTests {
    @Autowired
    @Qualifier("statelessTicketRegistryCipherExecutor")
    private CipherExecutor statelessTicketRegistryCipherExecutor;

    @Test
    void verifyStatelessCipher() {
        val value = UUID.randomUUID().toString();
        val encoded = statelessTicketRegistryCipherExecutor.encode(value.getBytes(StandardCharsets.UTF_8));
        val decoded = (byte[]) statelessTicketRegistryCipherExecutor.decode(encoded);
        assertEquals(value, new String(decoded, StandardCharsets.UTF_8));
    }

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
        assertTrue(validatedAssertion.getContext().containsKey(Principal.class.getName()));
        assertFalse(validatedAssertion.getContext().containsKey(TicketGrantingTicketImpl.class.getName()));

        val authentication = validatedAssertion.getPrimaryAuthentication();
        assertTrue(authentication.getSuccesses().containsKey(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(authentication.getAttributes().containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        assertTrue(authentication.getAttributes().containsKey(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE));
        assertTrue(authentication.getAttributes().containsKey(Credential.CREDENTIAL_TYPE_ATTRIBUTE));
        assertEquals("developer", authentication.getPrincipal().getId());
        val attributes = authentication.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("groupMembership"));
        assertTrue(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("eduPersonAffiliation"));
        assertTrue(attributes.containsKey("binaryAttribute"));
    }

    @ParameterizedTest
    @MethodSource("ticketProvider")
    void verifyTicketCompactors(final Ticket ticket) throws Throwable {
        val compactors = applicationContext.getBeansOfType(TicketCompactor.class).values();
        for (val compactor : compactors) {
            if (compactor.getTicketType().isAssignableFrom(ticket.getClass())) {
                val compacted = compactor.compact(ticket);
                assertNotNull(compacted);
                val expanded = compactor.expand(compacted);
                assertNotNull(expanded);
            }
        }
    }

    static Stream<Arguments> ticketProvider() {
        val service = RegisteredServiceTestUtils.getService("eduPersonTest");
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticketGrantingTicket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), authentication, NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(UUID.randomUUID().toString(),
            service, NeverExpiresExpirationPolicy.INSTANCE, true, TicketTrackingPolicy.noOp());

        val properties = new HashMap<String, Serializable>(CoreAuthenticationTestUtils.getAttributes());
        properties.put("url", RegisteredServiceTestUtils.CONST_TEST_URL2);
        val transientTicket = new TransientSessionTicketImpl(UUID.randomUUID().toString(),
            NeverExpiresExpirationPolicy.INSTANCE, service, properties);
        val proxyGrantingTicket = new ProxyGrantingTicketImpl(UUID.randomUUID().toString(),
            service, ticketGrantingTicket, authentication, NeverExpiresExpirationPolicy.INSTANCE);
        val proxyTicket = new ProxyTicketImpl(UUID.randomUUID().toString(),
            ticketGrantingTicket, service, false, NeverExpiresExpirationPolicy.INSTANCE);

        return Stream.of(
            arguments(Named.of(ticketGrantingTicket.getPrefix() + ' ' + ticketGrantingTicket.getId(), ticketGrantingTicket)),
            arguments(Named.of(serviceTicket.getPrefix() + ' ' + serviceTicket.getId(), serviceTicket)),
            arguments(Named.of(transientTicket.getPrefix() + ' ' + transientTicket.getId(), transientTicket)),
            arguments(Named.of(proxyGrantingTicket.getPrefix() + ' ' + proxyGrantingTicket.getId(), proxyGrantingTicket)),
            arguments(Named.of(proxyTicket.getPrefix() + ' ' + proxyTicket.getId(), proxyTicket))
        );
    }
}
