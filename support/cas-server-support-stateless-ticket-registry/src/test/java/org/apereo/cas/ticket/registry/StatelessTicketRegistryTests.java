package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasStatelessTicketRegistryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StatelessTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Tickets")
@Tag("TicketRegistryTestWithoutEncryption")
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@ImportAutoConfiguration(CasStatelessTicketRegistryAutoConfiguration.class)
@Getter
@TestPropertySource(properties = {
    "cas.ticket.registry.stateless.crypto.signing.key=classpath:/private.key",
    "cas.ticket.registry.stateless.crypto.encryption.key=classpath:/public.key"
})
class StatelessTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Override
    protected boolean canTicketRegistryIterate() {
        return false;
    }

    @Override
    protected boolean canTicketRegistryDelete() {
        return false;
    }

    @RepeatedTest(2)
    void verifyStatelessTickets() throws Exception {
        val expirationPolicy = new RememberMeDelegatingExpirationPolicy()
            .addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME,
                new TicketGrantingTicketExpirationPolicy(60000, 2000))
            .addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT,
                new TicketGrantingTicketExpirationPolicy(2000, 2000));

        val attributes = new HashMap<String, List<Object>>();
        IntStream.rangeClosed(1, 10).forEach(i -> attributes.put(RandomUtils.randomAlphabetic(1), List.of(RandomUtils.randomAlphabetic(10))));

        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);
        val originalAuthn = RegisteredServiceTestUtils.getAuthentication(principal, attributes);

        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val tgt = new TicketGrantingTicketImpl(ticketGrantingTicketId, originalAuthn, expirationPolicy);
        val addedTicketGrantingTicket = newTicketRegistry.addTicket(tgt);
        assertNotNull(addedTicketGrantingTicket.getExpirationPolicy());
        assertTrue(addedTicketGrantingTicket.isStateless());
        val foundTicketGrantingTicket = newTicketRegistry.getTicket(addedTicketGrantingTicket.getId());
        assertNotNull(foundTicketGrantingTicket);
        assertEquals(tgt, foundTicketGrantingTicket);

        val service = RegisteredServiceTestUtils.getService("https://apereo.github.io/cas");
        service.getAttributes().putAll(attributes);
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));

        val serviceTicket = tgt.grantServiceTicket(UUID.randomUUID().toString(),
            service,
            new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 100),
            true, TicketTrackingPolicy.noOp());
        val addedServiceTicket = newTicketRegistry.addTicket(serviceTicket);
        assertTrue(addedServiceTicket.isStateless());

        val retrievedTicket = (RenewableServiceTicket) newTicketRegistry.getTicket(addedServiceTicket.getId());
        assertNotNull(retrievedTicket);
        assertTrue(retrievedTicket.isStateless());
        assertTrue(retrievedTicket.isFromNewLogin());
        assertEquals(serviceTicket.getAuthentication().getPrincipal(), ((AuthenticationAwareTicket) retrievedTicket).getAuthentication().getPrincipal());
    }

    @RepeatedTest(2)
    void verifyTransientTickets() throws Throwable {
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val service = RegisteredServiceTestUtils.getService("https://apereo.github.io/cas");
        val transientTicket = transientFactory.create(service);
        val addedTicket = newTicketRegistry.addTicket(transientTicket);
        assertNotNull(newTicketRegistry.getTicket(addedTicket.getId()));
    }

    @RepeatedTest(2)
    void verifyLargeServiceUrl() throws Exception {
        val attributes = new HashMap<String, List<Object>>();
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(), attributes);
        val originalAuthn = RegisteredServiceTestUtils.getAuthentication(principal, attributes);

        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val tgt = new TicketGrantingTicketImpl(ticketGrantingTicketId, originalAuthn,
            new TicketGrantingTicketExpirationPolicy(5000, 2000));
        newTicketRegistry.addTicket(tgt);

        val paths = IntStream.rangeClosed(1, 10).mapToObj(i -> RandomUtils.randomAlphabetic(10)).collect(Collectors.joining("/"));
        val service = RegisteredServiceTestUtils.getService("https://apereo.github.io:8443/cas/" + paths + "/page.html");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));

        val serviceTicket = tgt.grantServiceTicket(UUID.randomUUID().toString(),
            service, new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 100),
            true, TicketTrackingPolicy.noOp());
        val addedServiceTicket = newTicketRegistry.addTicket(serviceTicket);
        assertTrue(addedServiceTicket.isStateless());

        val retrievedTicket = (RenewableServiceTicket) newTicketRegistry.getTicket(addedServiceTicket.getId());
        assertNotNull(retrievedTicket);
    }

}
