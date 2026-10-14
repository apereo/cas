package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasStatelessTicketRegistryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.CompactTicketAuthentication;
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
@Import({
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    BaseCasCoreTests.SharedTestConfiguration.PrincipalResolutionTestConfiguration.class
})
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
        assertEquals(addedTicketGrantingTicket.getId(), foundTicketGrantingTicket.getId());
        assertEquals(tgt.getAuthentication().getPrincipal(), ((AuthenticationAwareTicket) foundTicketGrantingTicket).getAuthentication().getPrincipal());

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
        val service = RegisteredServiceTestUtils.getService("https://localhost:8443/cas/idp/profile/SAML2/Callback?srid="
            + UUID.randomUUID() + "&entityId=" + URLEncoder.encode("https://sp.example.org/metadata/sp", StandardCharsets.UTF_8));
        val transientTicket = transientFactory.create(service);
        val addedTicket = newTicketRegistry.addTicket(transientTicket);
        val foundTicket = (TransientSessionTicket) newTicketRegistry.getTicket(addedTicket.getId());
        assertNotNull(foundTicket);
        assertEquals(service.getId(), Objects.requireNonNull(foundTicket.getService()).getId());
    }

    @RepeatedTest(2)
    void verifyTicketGrantingTicketIsFoundByItsStatelessId() throws Throwable {
        val tgt = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), new TicketGrantingTicketExpirationPolicy(5000, 2000));
        val addedTicket = newTicketRegistry.addTicket(tgt);
        val foundTicket = newTicketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertEquals(addedTicket.getId(), foundTicket.getId());
        assertEquals(foundTicket.getId(), newTicketRegistry.getTicket(foundTicket.getId()).getId());

        var ticketId = addedTicket.getId();
        for (var update = 0; update < 3; update++) {
            val updatedTicket = newTicketRegistry.updateTicket(newTicketRegistry.getTicket(ticketId, TicketGrantingTicket.class));
            assertTrue(updatedTicket.getId().length() < addedTicket.getId().length() * 2);
            ticketId = updatedTicket.getId();
        }
        assertEquals(ticketId, newTicketRegistry.getTicket(ticketId, TicketGrantingTicket.class).getId());
    }

    @RepeatedTest(2)
    void verifyServiceTicketFieldsCannotBeInjectedThroughService() throws Exception {
        val service = RegisteredServiceTestUtils.getService(
            "https://app.example.org/app,0,victim:InjectedHandler:InjectedCredential,0/callback");
        val retrievedTicket = grantAndRetrieveServiceTicket("mallory", service);
        assertNotNull(retrievedTicket);
        assertEquals("mallory", ((AuthenticationAwareTicket) retrievedTicket).getAuthentication().getPrincipal().getId());
        assertTrue(retrievedTicket.isFromNewLogin());
        assertEquals(service.getShortenedId(), ((ServiceAwareTicket) retrievedTicket).getService().getId());
    }

    @RepeatedTest(2)
    void verifyServiceTicketForDistinguishedNamePrincipal() throws Exception {
        val principalId = "CN=Jane Doe,OU=Staff:Faculty,O=Example";
        val retrievedTicket = grantAndRetrieveServiceTicket(principalId, RegisteredServiceTestUtils.getService("https://apereo.github.io/cas"));
        assertNotNull(retrievedTicket);
        assertEquals(principalId, ((AuthenticationAwareTicket) retrievedTicket).getAuthentication().getPrincipal().getId());
    }

    @RepeatedTest(2)
    void verifyExpandedTicketsCarryTheirStatelessIds() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication(RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString()),
            new HashMap<>(Map.<String, List<Object>>of(CompactTicketAuthentication.CLIENT_NAME_ATTRIBUTE, List.of("SAML2Client"),
                "authnContextClass", List.of("mfa-duo"), "unrelated", List.of("dropped"))));
        val tickets = newTicketsOfEveryType(authentication);
        val transientTicket = (TransientSessionTicket) tickets.getLast();

        for (val ticket : tickets) {
            val addedTicket = newTicketRegistry.addTicket(ticket);
            val foundTicket = newTicketRegistry.getTicket(addedTicket.getId());
            assertNotNull(foundTicket, () -> "Ticket not found: " + ticket.getPrefix());
            assertEquals(addedTicket.getId(), foundTicket.getId());
            assertEquals(ticket.getCreationTime().toEpochSecond(), foundTicket.getCreationTime().toEpochSecond());
            assertEquals(ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond(),
                foundTicket.getExpirationPolicy().toMaximumExpirationTime(foundTicket).toEpochSecond());
            assertEquals(ticket.getClass(), foundTicket.getClass());
            if (foundTicket instanceof final AuthenticationAwareTicket authenticationAware && authenticationAware.getAuthentication() != null) {
                val expandedAuthentication = authenticationAware.getAuthentication();
                assertEquals(authentication.getPrincipal().getId(), expandedAuthentication.getPrincipal().getId());
                assertEquals(authentication.getAuthenticationDate().toEpochSecond(),
                    expandedAuthentication.getAuthenticationDate().toEpochSecond());
                assertEquals(authentication.getSuccesses().keySet(), expandedAuthentication.getSuccesses().keySet());
                assertEquals(List.of("SAML2Client"), expandedAuthentication.getAttributes().get(CompactTicketAuthentication.CLIENT_NAME_ATTRIBUTE));
                assertEquals(List.of("mfa-duo"), expandedAuthentication.getAttributes().get("authnContextClass"));
                if (!(foundTicket instanceof TicketGrantingTicket)) {
                    assertFalse(expandedAuthentication.getAttributes().containsKey("unrelated"));
                }
            }
        }
        val foundTransientTicket = (TransientSessionTicket) newTicketRegistry.getTicket(newTicketRegistry.addTicket(transientTicket).getId());
        assertEquals("value", foundTransientTicket.getProperties().get("key"));
        assertEquals(List.of("a,b", "c:d"), foundTransientTicket.getProperties().get("keys"));
    }

    @RepeatedTest(2)
    void verifyTicketsCannotBeRelabelledAsAnotherType() throws Throwable {
        val tickets = newTicketsOfEveryType(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()));
        val addedIds = new ArrayList<String>();
        for (val ticket : tickets) {
            addedIds.add(newTicketRegistry.addTicket(ticket).getId());
        }
        for (val addedId : addedIds) {
            val encoded = addedId.substring(addedId.indexOf(UniqueTicketIdGenerator.SEPARATOR));
            assertNotNull(newTicketRegistry.getTicket(addedId));
            for (val ticket : tickets) {
                if (!addedId.startsWith(ticket.getPrefix() + UniqueTicketIdGenerator.SEPARATOR)) {
                    assertNull(newTicketRegistry.getTicket(ticket.getPrefix() + encoded),
                        () -> addedId + " was accepted as " + ticket.getPrefix());
                }
            }
        }
    }

    @RepeatedTest(2)
    void verifyTicketGrantingTicketDoesNotCarryTrackedTickets() throws Exception {
        val tgt = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), new TicketGrantingTicketExpirationPolicy(5000, 2000));
        val addedTicket = newTicketRegistry.addTicket(tgt);
        val foundTicket = newTicketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        IntStream.rangeClosed(1, 20).forEach(index -> {
            foundTicket.getServices().put(UUID.randomUUID().toString(), RegisteredServiceTestUtils.getService("https://app" + index + ".example.org"));
            foundTicket.getDescendantTickets().add(UUID.randomUUID().toString());
        });
        foundTicket.update();
        val updatedTicket = newTicketRegistry.updateTicket(foundTicket);
        val expandedTicket = newTicketRegistry.getTicket(updatedTicket.getId(), TicketGrantingTicket.class);
        assertTrue(expandedTicket.getServices().isEmpty());
        assertTrue(expandedTicket.getDescendantTickets().isEmpty());
        assertEquals(tgt.getCreationTime().toEpochSecond(), expandedTicket.getCreationTime().toEpochSecond());
        assertEquals(tgt.getAuthentication().getPrincipal(), expandedTicket.getAuthentication().getPrincipal());
        assertTrue(updatedTicket.getId().length() < addedTicket.getId().length() * 3 / 2);
    }

    @RepeatedTest(2)
    void verifyTicketGrantingTicketResolvesPrincipalAttributes() throws Exception {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            new HashMap<>(Map.<String, List<Object>>of("nickname", List.of(RandomUtils.randomAlphabetic(8192)))));
        val authentication = DefaultAuthenticationBuilder
            .newInstance(RegisteredServiceTestUtils.getAuthentication(principal, new HashMap<>(Map.<String, List<Object>>of("authnContext", List.of("mfa-simple")))))
            .addSuccess("principalHandler", new DefaultAuthenticationHandlerExecutionResult("principalHandler",
                new UsernamePasswordCredential(), principal, new ArrayList<>()))
            .build();
        val tgt = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            authentication, new TicketGrantingTicketExpirationPolicy(5000, 2000));
        val addedTicket = newTicketRegistry.addTicket(tgt);
        assertTrue(addedTicket.getId().length() < 4096);

        val expandedTicket = newTicketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertEquals(tgt.getCreationTime().toEpochSecond(), expandedTicket.getCreationTime().toEpochSecond());
        assertEquals(tgt.getExpirationPolicy().toMaximumExpirationTime(tgt).toEpochSecond(),
            expandedTicket.getExpirationPolicy().toMaximumExpirationTime(expandedTicket).toEpochSecond());
        val expandedAuthentication = expandedTicket.getAuthentication();
        val expandedPrincipal = expandedAuthentication.getPrincipal();
        assertEquals(principal.getId(), expandedPrincipal.getId());
        assertFalse(expandedPrincipal.containsAttribute("nickname"));
        assertEquals(List.of("cas@apereo.org"), expandedPrincipal.getAttributes().get("mail"));
        assertEquals(List.of("mfa-simple"), expandedAuthentication.getAttributes().get("authnContext"));
        assertEquals(authentication.getSuccesses().keySet(), expandedAuthentication.getSuccesses().keySet());
        assertEquals(1, expandedAuthentication.getCredentials().size());
        assertTrue(Objects.requireNonNull(expandedAuthentication.getSuccesses().get("principalHandler").getPrincipal()).getAttributes().isEmpty());
    }

    private List<Ticket> newTicketsOfEveryType(final Authentication authentication) throws Throwable {
        val service = RegisteredServiceTestUtils.getService("https://apereo.github.io/cas");
        val tgt = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            authentication, new TicketGrantingTicketExpirationPolicy(5000, 2000));
        val serviceTicket = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket(UUID.randomUUID().toString(), service,
            new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 100), true, TicketTrackingPolicy.noOp());
        val proxyGrantingTicket = serviceTicket.grantProxyGrantingTicket(TestTicketIdentifiers.generate().proxyGrantingTicketId(),
            authentication, new TicketGrantingTicketExpirationPolicy(5000, 2000), TicketTrackingPolicy.noOp());
        val proxyTicket = proxyGrantingTicket.grantProxyTicket(UUID.randomUUID().toString(), service,
            new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(1, 100), TicketTrackingPolicy.noOp());
        val transientTicket = ((TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class))
            .create(service, new HashMap<>(Map.of("key", "value", "keys", new ArrayList<>(List.of("a,b", "c:d")))));
        return List.of(tgt, serviceTicket, proxyGrantingTicket, proxyTicket, transientTicket);
    }

    private RenewableServiceTicket grantAndRetrieveServiceTicket(final String principalId,
                                                                 final Service service)
            throws Exception {
        val authentication = RegisteredServiceTestUtils.getAuthentication(RegisteredServiceTestUtils.getPrincipal(principalId));
        val ticketGrantingTicket = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            authentication, new TicketGrantingTicketExpirationPolicy(5000, 2000));
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(UUID.randomUUID().toString(), service,
            new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(1, 100), false, TicketTrackingPolicy.noOp());
        val addedServiceTicket = newTicketRegistry.addTicket(serviceTicket);
        return (RenewableServiceTicket) newTicketRegistry.getTicket(addedServiceTicket.getId());
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
