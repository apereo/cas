package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class ServiceTicketImplTests extends BaseTicketFactoryTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).writeDatesAsTimestamps(true).build().toObjectMapper();

    private static final String ST_ID = UUID.randomUUID().toString();

    private static final File ST_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "st.json");

    private static final String ID = UUID.randomUUID().toString();

    private TicketGrantingTicket tgt;

    private final DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    @BeforeEach
    public void initialize() throws Throwable {
        tgt = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
    }

    @Test
    void verifySerializeToJson() throws IOException {
        val stWritten = new ServiceTicketImpl(ST_ID, tgt, RegisteredServiceTestUtils.getService(), true, NeverExpiresExpirationPolicy.INSTANCE);
        MAPPER.writeValue(ST_JSON_FILE, stWritten);
        val stRead = MAPPER.readValue(ST_JSON_FILE, ServiceTicketImpl.class);
        assertEquals(stWritten, stRead);
    }

    @Test
    void verifyNoService() throws Throwable {
        assertThrows(Exception.class, () -> new ServiceTicketImpl(ST_ID, tgt, null, false, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    void verifyIsFromNewLoginTrue() throws Throwable {
        val serviceTicket = new ServiceTicketImpl(ST_ID, tgt, CoreAuthenticationTestUtils.getService(),
            true, NeverExpiresExpirationPolicy.INSTANCE);
        assertTrue(serviceTicket.isFromNewLogin());
    }

    @Test
    void verifyIsFromNewLoginFalse() throws Throwable {
        val serviceTicket = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertTrue(serviceTicket.isFromNewLogin());
        val renewableServiceTicket = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertFalse(renewableServiceTicket.isFromNewLogin());
    }

    @Test
    void verifyGetService() throws Throwable {
        val simpleService = CoreAuthenticationTestUtils.getService();
        val serviceTicket = new ServiceTicketImpl(ST_ID, tgt, simpleService, false,
            NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(simpleService, serviceTicket.getService());
    }

    @Test
    void verifyGetTicket() throws Throwable {
        val simpleService = CoreAuthenticationTestUtils.getService();
        val serviceTicket = new ServiceTicketImpl(ST_ID, tgt, simpleService, false,
            NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(tgt, serviceTicket.getTicketGrantingTicket());
    }

    @Test
    void verifyTicketNeverExpires() throws Throwable {
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX),
            CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE,
            false, serviceTicketSessionTrackingPolicy);
        ticketGrantingTicket.markTicketExpired();
        assertFalse(serviceTicket.isExpired());
    }

    @Test
    void verifyIsExpiredFalse() throws Throwable {
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, serviceTicketSessionTrackingPolicy);
        assertFalse(serviceTicket.isExpired());
    }

    @Test
    void verifyTicketGrantingTicket() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket =
            (ProxyGrantingTicketIssuerTicket) ticketGrantingTicket.grantServiceTicket(
                idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
                false, serviceTicketSessionTrackingPolicy);
        val t1 = serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), authentication,
            NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(authentication, t1.getAuthentication());
    }

    @Test
    void verifyTicketWithoutTicketGrantingTicket() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val serviceTicket = ((ServiceTicketFactory) ticketFactory.get(ServiceTicket.class)).create(CoreAuthenticationTestUtils.getService(),
            authentication, true, ServiceTicket.class);
        assertNotNull(serviceTicket);
        assertEquals(serviceTicket.getAuthentication(), authentication);
    }

    @Test
    void verifyTicketGrantingTicketGrantedTwice() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket =
            (ProxyGrantingTicketIssuerTicket) ticketGrantingTicket.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
                false, serviceTicketSessionTrackingPolicy);
        serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
            authentication, NeverExpiresExpirationPolicy.INSTANCE);
        assertThrows(Exception.class,
            () -> serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, NeverExpiresExpirationPolicy.INSTANCE));
    }
}
