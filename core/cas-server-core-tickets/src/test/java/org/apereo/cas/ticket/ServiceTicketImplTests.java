package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
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

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
public class ServiceTicketImplTests {

    private static final String ST_ID = "stest1";

    private static final File ST_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "st.json");

    private static final String ID = "test";

    private final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl(ID,
        CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

    private final DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    private ObjectMapper mapper;

    @BeforeEach
    public void initialize() {
        mapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        mapper.findAndRegisterModules();
    }

    @Test
    public void verifySerializeToJson() throws IOException {
        val stWritten = new ServiceTicketImpl(ST_ID, tgt, RegisteredServiceTestUtils.getService(), true, NeverExpiresExpirationPolicy.INSTANCE);

        mapper.writeValue(ST_JSON_FILE, stWritten);
        val stRead = mapper.readValue(ST_JSON_FILE, ServiceTicketImpl.class);
        assertEquals(stWritten, stRead);
    }

    @Test
    public void verifyNoService() {
        assertThrows(Exception.class, () -> new ServiceTicketImpl(ST_ID, tgt, null, false, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    public void verifyNoTicket() {
        assertThrows(NullPointerException.class,
            () -> new ServiceTicketImpl(ST_ID, null, CoreAuthenticationTestUtils.getService(), false, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    public void verifyIsFromNewLoginTrue() {
        val s = new ServiceTicketImpl(ST_ID, tgt, CoreAuthenticationTestUtils.getService(), true, NeverExpiresExpirationPolicy.INSTANCE);

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyIsFromNewLoginFalse() {
        val s = tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE, false, false);
        assertTrue(s.isFromNewLogin());
        val s1 = tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE, false, false);
        assertFalse(s1.isFromNewLogin());
    }

    @Test
    public void verifyGetService() {
        val simpleService = CoreAuthenticationTestUtils.getService();
        val s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false, NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(simpleService, s.getService());
    }

    @Test
    public void verifyGetTicket() {
        val simpleService = CoreAuthenticationTestUtils.getService();
        val s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false, NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(tgt, s.getTicketGrantingTicket());
    }

    @Test
    public void verifyTicketNeverExpires() {
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX),
            CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE,
            false, true);
        t.markTicketExpired();
        assertFalse(s.isExpired());
    }

    @Test
    public void verifyIsExpiredFalse() {
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);

        assertFalse(s.isExpired());
    }

    @Test
    public void verifyTicketGrantingTicket() throws AbstractTicketException {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        val t1 = s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
            NeverExpiresExpirationPolicy.INSTANCE);

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    public void verifyTicketGrantingTicketGrantedTwice() throws AbstractTicketException {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
            new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a, NeverExpiresExpirationPolicy.INSTANCE);

        assertThrows(Exception.class,
            () -> s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a, NeverExpiresExpirationPolicy.INSTANCE));
    }
}
