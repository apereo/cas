package org.apereo.cas.ticket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class TicketGrantingTicketImplTests {

    private static final File TGT_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "tgt.json");
    private static final String TGT_ID = "test";
    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        final Authentication authenticationWritten = CoreAuthenticationTestUtils.getAuthentication();
        final NeverExpiresExpirationPolicy expirationPolicyWritten = new NeverExpiresExpirationPolicy();
        final TicketGrantingTicket tgtWritten = new TicketGrantingTicketImpl(TGT_ID, null, null, 
                authenticationWritten, expirationPolicyWritten);

        mapper.writeValue(TGT_JSON_FILE, tgtWritten);
        final TicketGrantingTicketImpl tgtRead = mapper.readValue(TGT_JSON_FILE, TicketGrantingTicketImpl.class);
        assertEquals(tgtWritten, tgtRead);
        assertEquals(authenticationWritten, tgtRead.getAuthentication());
    }

    @Test
    public void verifyEquals() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertNotNull(t);
        assertFalse(t.equals(new Object()));
        assertTrue(t.equals(t));
    }

    @Test
    public void verifyNullAuthentication() {
        this.thrown.expect(Exception.class);
        new TicketGrantingTicketImpl(TGT_ID, null, null, null, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyGetAuthentication() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication();

        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null, authentication, new NeverExpiresExpirationPolicy());

        assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    @Test
    public void verifyIsRootTrue() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    @Test
    public void verifyIsRootFalse() {
        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID,
                CoreAuthenticationTestUtils.getService("gantor"), t1,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    @Test
    public void verifyProperRootIsReturned() {
        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t2 = new TicketGrantingTicketImpl(TGT_ID,
                CoreAuthenticationTestUtils.getService("gantor"), t1,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        final TicketGrantingTicket t3 = new TicketGrantingTicketImpl(TGT_ID,
                CoreAuthenticationTestUtils.getService("gantor"), t2,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertSame(t1, t3.getRoot());
    }

    @Test
    public void verifyGetChainedPrincipalsWithOne() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);

        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyCheckCreationTime() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);

        final ZonedDateTime startTime = ZonedDateTime.now(ZoneOffset.UTC).minusNanos(100);
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication, new NeverExpiresExpirationPolicy());
        final ZonedDateTime finishTime = ZonedDateTime.now(ZoneOffset.UTC).plusNanos(100);
        assertTrue(startTime.isBefore(t.getCreationTime()) && finishTime.isAfter(t.getCreationTime()));
    }

    @Test
    public void verifyGetChainedPrincipalsWithTwo() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication();
        final Authentication authentication1 = CoreAuthenticationTestUtils.getAuthentication("test1");
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);
        principals.add(authentication1);

        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication1, new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID,
                CoreAuthenticationTestUtils.getService("gantor"), t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyServiceTicketAsFromInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(ID_GENERATOR
            .getNewTicketId(ServiceTicket.PREFIX), RegisteredServiceTestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false, true);

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyServiceTicketAsFromNotInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        final ServiceTicket s = t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyWebApplicationServices() {
        final Service testService = RegisteredServiceTestUtils.getService(TGT_ID);
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(ID_GENERATOR
            .getNewTicketId(ServiceTicket.PREFIX), testService,
            new NeverExpiresExpirationPolicy(), false, true);
        Map<String, Service> services = t.getServices();
        assertEquals(1, services.size());
        final String ticketId = services.keySet().iterator().next();
        assertEquals(testService, services.get(ticketId));
        t.removeAllServices();
        services = t.getServices();
        assertEquals(0, services.size());
    }

    @Test
    public void verifyWebApplicationExpire() {
        final Service testService = RegisteredServiceTestUtils.getService(TGT_ID);
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(ID_GENERATOR
                        .getNewTicketId(ServiceTicket.PREFIX), testService,
                new NeverExpiresExpirationPolicy(), false, true);
        assertFalse(t.isExpired());
        t.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com?test"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com;JSESSIONID=xxx"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceWithPathTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1?test=true"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepAll() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                false);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceTicket() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService2(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceOnPathTicket() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl(TGT_ID, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp2"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(2, t.getServices().size());
    }
}
