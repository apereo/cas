package org.apereo.cas.ticket;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ServiceTicketImplTests {

    public static final String ST_ID = "stest1";
    private final TicketGrantingTicketImpl ticketGrantingTicket = new TicketGrantingTicketImpl("test",
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

    private static final File ST_JSON_FILE = new File("st.json");
    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        // needed in order to serialize ZonedDateTime class
        mapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.findAndRegisterModules();
    }

    @Test
    public void verifySerializeToJson() throws IOException {
        final ServiceTicket stWritten = new ServiceTicketImpl(ST_ID, this.ticketGrantingTicket,
                org.apereo.cas.services.TestUtils.getService(), true,
                new NeverExpiresExpirationPolicy());


        mapper.writeValue(ST_JSON_FILE, stWritten);

        final ServiceTicketImpl stRead = mapper.readValue(ST_JSON_FILE, ServiceTicketImpl.class);

        assertEquals(stWritten, stRead);
//        assertEquals(authenticationWitten, tgtRead.getAuthentication());
    }
    @Test(expected = Exception.class)
    public void verifyNoService() {
        new ServiceTicketImpl(ST_ID, this.ticketGrantingTicket, null, false, new NeverExpiresExpirationPolicy());
    }

    @Test(expected = Exception.class)
    public void verifyNoTicket() {
        new ServiceTicketImpl(ST_ID, null, org.apereo.cas.services.TestUtils.getService(), false, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyIsFromNewLoginTrue() {
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, this.ticketGrantingTicket,
                org.apereo.cas.services.TestUtils.getService(), true,
                new NeverExpiresExpirationPolicy());
        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyIsFromNewLoginFalse() {
        ServiceTicket s = this.ticketGrantingTicket.grantServiceTicket(ST_ID,
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, false);

        assertTrue(s.isFromNewLogin());
        
        s = this.ticketGrantingTicket.grantServiceTicket(ST_ID, 
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, false);
        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyGetService() {
        final Service simpleService = org.apereo.cas.services.TestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        Assert.assertEquals(simpleService, s.getService());
    }

    @Test
    public void verifyGetTicket() {
        final Service simpleService = org.apereo.cas.services.TestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl(ST_ID, this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        assertEquals(this.ticketGrantingTicket, s.getGrantingTicket());
    }

    @Test
    public void verifyIsExpiredTrueBecauseOfRoot() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(), new NeverExpiresExpirationPolicy(), false, true);
        t.markTicketExpired();

        assertTrue(s.isExpired());
    }

    @Test
    public void verifyIsExpiredFalse() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        assertFalse(s.isExpired());
    }

    @Test
    public void verifyTicketGrantingTicket() throws AbstractTicketException {
        final Authentication a = TestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        final TicketGrantingTicket t1 = s.grantProxyGrantingTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        Assert.assertEquals(a, t1.getAuthentication());
    }

    @Test
    public void verifyTicketGrantingTicketGrantedTwice() throws AbstractTicketException {
        final Authentication a = TestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        s.grantProxyGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        try {
            s.grantProxyGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                    new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}
