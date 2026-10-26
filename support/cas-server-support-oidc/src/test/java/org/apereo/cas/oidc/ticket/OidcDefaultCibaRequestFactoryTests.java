package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.StatelessTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultCibaRequestFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class OidcDefaultCibaRequestFactoryTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(TicketCatalog.BEAN_NAME)
    private TicketCatalog ticketCatalog;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Test
    void verifyOperation() throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .clientId(UUID.randomUUID().toString())
            .clientNotificationToken(UUID.randomUUID().toString())
            .bindingMessage(UUID.randomUUID().toString())
            .resources(Set.of("https://api.example.org/"))
            .build();
        val factory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val ticket = factory.create(cibaRequestContext);
        assertNotNull(ticket);
        assertTrue(ticket.getId().startsWith(OidcCibaRequest.PREFIX));
        assertSame(OidcCibaRequest.class, factory.getTicketType());
        assertEquals(ticket.getClientId(), cibaRequestContext.getClientId());
        assertEquals(Set.of("https://api.example.org/"), ticket.getResources());
        val authentication = ticket.getAuthentication();
        assertEquals(authentication.getAttributes().get(OidcConstants.CLIENT_NOTIFICATION_TOKEN).getFirst(), cibaRequestContext.getClientNotificationToken());
        assertEquals(authentication.getAttributes().get(OidcConstants.BINDING_MESSAGE).getFirst(), cibaRequestContext.getBindingMessage());
    }

    @Test
    void verifyOperationWithExpiry() throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .requestedExpiry(30)
            .build();
        val factory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val ticket = factory.create(cibaRequestContext);
        assertNotNull(ticket);
        assertTrue(ticket.getId().startsWith(OidcCibaRequest.PREFIX));
        assertSame(OidcCibaRequest.class, factory.getTicketType());
    }

    @Test
    void verifyStatelessRequestIsGivenItsOwnId() throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .clientId(UUID.randomUUID().toString())
            .clientNotificationToken(UUID.randomUUID().toString())
            .bindingMessage(UUID.randomUUID().toString())
            .userCode(UUID.randomUUID().toString())
            .resources(Set.of("https://api.example.org/"))
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .tenant("shire")
            .build();
        val factory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val ticket = factory.create(cibaRequestContext);
        assertNotEquals(ticket.getId(), ticket.getEncodedId());

        val compactor = new OidcCibaRequestCompactor(new DirectObjectProvider<>(defaultTicketFactory),
            PrincipalFactoryUtils.newPrincipalFactory(), new DirectObjectProvider<>(oidcConfigurationContext.getPrincipalResolver()));
        val registry = new StatelessTicketRegistry((CipherExecutor) CipherExecutor.noOp(), ticketSerializationManager,
            ticketCatalog, applicationContext, List.of(compactor));
        val storedTicket = Objects.requireNonNull(registry.addTicket(ticket));
        assertTrue(storedTicket.isStateless());
        assertTrue(storedTicket.getId().startsWith(OidcCibaRequest.PREFIX));

        val expanded = Objects.requireNonNull(registry.getTicket(storedTicket.getId(), OidcCibaRequest.class));
        assertEquals(storedTicket.getId(), expanded.getEncodedId());
        assertEquals(ticket.getClientId(), expanded.getClientId());
        assertEquals(ticket.getScopes(), expanded.getScopes());
        assertEquals(ticket.getResources(), expanded.getResources());
        assertEquals("shire", expanded.getTenantId());
        assertEquals("casuser", expanded.getAuthentication().getPrincipal().getId());
        val attributes = expanded.getAuthentication().getAttributes();
        assertEquals(cibaRequestContext.getClientNotificationToken(), attributes.get(OidcConstants.CLIENT_NOTIFICATION_TOKEN).getFirst());
        assertEquals(cibaRequestContext.getBindingMessage(), attributes.get(OidcConstants.BINDING_MESSAGE).getFirst());
        assertEquals(cibaRequestContext.getUserCode(), attributes.get(OidcConstants.USER_CODE).getFirst());
        assertEquals(ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond(),
            expanded.getExpirationPolicy().toMaximumExpirationTime(expanded).toEpochSecond());
    }
}
