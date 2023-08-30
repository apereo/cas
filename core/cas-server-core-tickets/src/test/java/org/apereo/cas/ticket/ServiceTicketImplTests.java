package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
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

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
class ServiceTicketImplTests {

    private static final String ST_ID = "stest1";

    private static final File ST_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "st.json");

    private static final String ID = "test";

    private TicketGrantingTicketImpl tgt;

    private final DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    private ObjectMapper mapper;

    private static ServiceTicketSessionTrackingPolicy getTrackingPolicy() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getCore().setOnlyTrackMostRecentSession(true);
        return new DefaultServiceTicketSessionTrackingPolicy(props,
            new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog()));
    }

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
        val s = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, getTrackingPolicy());
        assertTrue(s.isFromNewLogin());
        val s1 = (RenewableServiceTicket) tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, getTrackingPolicy());
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
        val s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false,
            NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(tgt, s.getTicketGrantingTicket());
    }

    @Test
    void verifyTicketNeverExpires() throws Throwable {
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX),
            CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE,
            false, getTrackingPolicy());
        t.markTicketExpired();
        assertFalse(s.isExpired());
    }

    @Test
    void verifyIsExpiredFalse() throws Throwable {
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, getTrackingPolicy());

        assertFalse(s.isExpired());
    }

    @Test
    void verifyTicketGrantingTicket() throws Throwable {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = (ProxyGrantingTicketIssuerTicket) t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, getTrackingPolicy());
        val t1 = s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
            NeverExpiresExpirationPolicy.INSTANCE);

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    void verifyTicketGrantingTicketGrantedTwice() throws Throwable {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = (ProxyGrantingTicketIssuerTicket) t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000),
            false, getTrackingPolicy());
        s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
            a, NeverExpiresExpirationPolicy.INSTANCE);
        assertThrows(Exception.class,
            () -> s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                a, NeverExpiresExpirationPolicy.INSTANCE));
    }
}
