package org.apereo.cas.ticket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ServiceTicketImplTests {

    private static final String ST_ID = "stest1";
    private static final File ST_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "st.json");
    private static final String ID = "test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl(ID,
            CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
    private final UniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        // needed in order to serialize ZonedDateTime class
        mapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.findAndRegisterModules();
    }

    @Test
    public void verifySerializeToJson() throws IOException {
        final ServiceTicket stWritten = new ServiceTicketImpl(ST_ID, tgt, RegisteredServiceTestUtils.getService(), true, new NeverExpiresExpirationPolicy());

        mapper.writeValue(ST_JSON_FILE, stWritten);
        final ServiceTicketImpl stRead = mapper.readValue(ST_JSON_FILE, ServiceTicketImpl.class);
        assertEquals(stWritten, stRead);
    }

    @Test
    public void verifyNoService() {
        this.thrown.expect(Exception.class);
        this.thrown.expectMessage("service cannot be null");

        new ServiceTicketImpl(ST_ID, tgt, null, false, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyNoTicket() {
        this.thrown.expect(Exception.class);
        this.thrown.expectMessage("ticket cannot be null");

        new ServiceTicketImpl(ST_ID, null, CoreAuthenticationTestUtils.getService(), false, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyIsFromNewLoginTrue() {
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, tgt, CoreAuthenticationTestUtils.getService(), true, new NeverExpiresExpirationPolicy());

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyIsFromNewLoginFalse() {
        ServiceTicket s = tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, false);
        assertTrue(s.isFromNewLogin());
        s = tgt.grantServiceTicket(ST_ID, CoreAuthenticationTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, false);
        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyGetService() {
        final Service simpleService = CoreAuthenticationTestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false, new NeverExpiresExpirationPolicy());
        assertEquals(simpleService, s.getService());
    }

    @Test
    public void verifyGetTicket() {
        final Service simpleService = CoreAuthenticationTestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, tgt, simpleService, false, new NeverExpiresExpirationPolicy());
        assertEquals(tgt, s.getGrantingTicket());
    }

    @Test
    public void verifyIsExpiredTrueBecauseOfRoot() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX),
                CoreAuthenticationTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, true);

        t.markTicketExpired();

        assertTrue(s.isExpired());
    }

    @Test
    public void verifyIsExpiredFalse() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);

        assertFalse(s.isExpired());
    }

    @Test
    public void verifyTicketGrantingTicket() throws AbstractTicketException {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        final TicketGrantingTicket t1 = s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    public void verifyTicketGrantingTicketGrantedTwice() throws AbstractTicketException {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(idGenerator.getNewTicketId(ServiceTicket.PREFIX), CoreAuthenticationTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a, new NeverExpiresExpirationPolicy());

        this.thrown.expect(Exception.class);

        s.grantProxyGrantingTicket(idGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a, new NeverExpiresExpirationPolicy());
    }
}
