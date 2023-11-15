package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class ServiceTicketImplTests extends BaseTicketFactoryTests {

    private static final String ST_ID = "stest1";

    private static final File ST_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "st.json");

    private static final String ID = "test";

    private TicketGrantingTicketImpl tgt;

    private final DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    private ObjectMapper mapper;

    @BeforeEach
    public void initialize() throws Throwable {
        tgt = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        mapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        mapper.findAndRegisterModules();
    }

    @Test
    void verifySerializeToJson() throws IOException {
        val stWritten = new ServiceTicketImpl(ST_ID, tgt, RegisteredServiceTestUtils.getService(), true, NeverExpiresExpirationPolicy.INSTANCE);
        mapper.writeValue(ST_JSON_FILE, stWritten);
        val stRead = mapper.readValue(ST_JSON_FILE, ServiceTicketImpl.class);
        assertEquals(stWritten, stRead);
    }

    @Test
    void verifyNoService() throws Throwable {
        assertThrows(Exception.class, () -> new ServiceTicketImpl(ST_ID, tgt, null, false, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    void verifyNoTicket() throws Throwable {
        assertThrows(NullPointerException.class,
            () -> new ServiceTicketImpl(ST_ID, null, CoreAuthenticationTestUtils.getService(), false, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    void verifyIsFromNewLoginTrue() throws Throwable {
        val s = new ServiceTicketImpl(ST_ID, tgt, CoreAuthenticationTestUtils.getService(),
            true, NeverExpiresExpirationPolicy.INSTANCE);
        assertTrue(s.isFromNewLogin());
    }

    @Test
    void verifyIsFromNewLoginFalse() throws Throwable {
        val serviceTicket = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertTrue(serviceTicket.isFromNewLogin());
        val s1 = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertFalse(s1.isFromNewLogin());
    }

    @Test
    void verifyGetService() throws Throwable {
        val simpleService = CoreAuthenticationTestUtils.getService();
        val s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false,
            NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(simpleService, s.getService());
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
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX),
            CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE,
            false, serviceTicketSessionTrackingPolicy);
        t.markTicketExpired();
        assertFalse(serviceTicket.isExpired());
    }

    @Test
    void verifyIsExpiredFalse() throws Throwable {
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, serviceTicketSessionTrackingPolicy);
        assertFalse(serviceTicket.isExpired());
    }

    @Test
    void verifyTicketGrantingTicket() throws Throwable {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = (ProxyGrantingTicketIssuerTicket) t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, serviceTicketSessionTrackingPolicy);
        val t1 = serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
            NeverExpiresExpirationPolicy.INSTANCE);

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    void verifyTicketGrantingTicketGrantedTwice() throws Throwable {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val serviceTicket = (ProxyGrantingTicketIssuerTicket) t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, serviceTicketSessionTrackingPolicy);
        serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
            a, NeverExpiresExpirationPolicy.INSTANCE);
        assertThrows(Exception.class,
            () -> serviceTicket.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                a, NeverExpiresExpirationPolicy.INSTANCE));
    }
}
