package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class TicketGrantingTicketImplTests {

    private static final File TGT_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "tgt.json");
    private static final String TGT_ID = "test";
    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

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
        val authenticationWritten = CoreAuthenticationTestUtils.getAuthentication();
        val expirationPolicyWritten = NeverExpiresExpirationPolicy.INSTANCE;
        val tgtWritten = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authenticationWritten, expirationPolicyWritten);

        mapper.writeValue(TGT_JSON_FILE, tgtWritten);
        val tgtRead = mapper.readValue(TGT_JSON_FILE, TicketGrantingTicketImpl.class);
        assertEquals(tgtWritten, tgtRead);
        assertEquals(authenticationWritten, tgtRead.getAuthentication());
    }

    @Test
    public void verifyEquals() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        assertNotNull(t);
        assertNotEquals(t, new Object());
        assertEquals(t, t);
    }

    @Test
    public void verifyNullAuthentication() {
        assertThrows(Exception.class, () -> new TicketGrantingTicketImpl(TGT_ID, null, null, null, NeverExpiresExpirationPolicy.INSTANCE));
    }

    @Test
    public void verifyGetAuthentication() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null, authentication, NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    @Test
    public void verifyIsRootTrue() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        assertTrue(t.isRoot());
    }

    @Test
    public void verifyIsRootFalse() {
        val t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val t = new TicketGrantingTicketImpl(TGT_ID,
            CoreAuthenticationTestUtils.getService("gantor"), t1,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        assertFalse(t.isRoot());
    }

    @Test
    public void verifyProperRootIsReturned() {
        val t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val t2 = new TicketGrantingTicketImpl(TGT_ID,
            CoreAuthenticationTestUtils.getService("gantor"), t1,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        val t3 = new TicketGrantingTicketImpl(TGT_ID,
            CoreAuthenticationTestUtils.getService("gantor"), t2,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        assertSame(t1, t3.getRoot());
    }

    @Test
    public void verifyGetChainedPrincipalsWithOne() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val principals = new ArrayList<Authentication>();
        principals.add(authentication);

        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication, NeverExpiresExpirationPolicy.INSTANCE);

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyCheckCreationTime() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();

        val startTime = ZonedDateTime.now(ZoneOffset.UTC).minusNanos(100);
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication, NeverExpiresExpirationPolicy.INSTANCE);
        val finishTime = ZonedDateTime.now(ZoneOffset.UTC).plusNanos(100);
        assertTrue(startTime.isBefore(t.getCreationTime()) && finishTime.isAfter(t.getCreationTime()));
    }

    @Test
    public void verifyGetChainedPrincipalsWithTwo() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val authentication1 = CoreAuthenticationTestUtils.getAuthentication("test1");
        val principals = new ArrayList<Authentication>();
        principals.add(authentication);
        principals.add(authentication1);

        val t1 = new TicketGrantingTicketImpl(TGT_ID, null, null,
            authentication1, NeverExpiresExpirationPolicy.INSTANCE);
        val t = new TicketGrantingTicketImpl(TGT_ID,
            CoreAuthenticationTestUtils.getService("gantor"), t1,
            authentication, NeverExpiresExpirationPolicy.INSTANCE);

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyServiceTicketAsFromInitialCredentials() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val s = t.grantServiceTicket(ID_GENERATOR
                .getNewTicketId(ServiceTicket.PREFIX), RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, true);

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyServiceTicketAsFromNotInitialCredentials() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        val s = t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyWebApplicationServices() {
        val testService = RegisteredServiceTestUtils.getService(TGT_ID);
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        t.grantServiceTicket(ID_GENERATOR
                .getNewTicketId(ServiceTicket.PREFIX), testService,
            NeverExpiresExpirationPolicy.INSTANCE, false, true);
        val services = t.getServices();
        assertEquals(1, services.size());
        val ticketId = services.keySet().iterator().next();
        assertEquals(testService, services.get(ticketId));
        t.removeAllServices();
        val services2 = t.getServices();
        assertEquals(0, services2.size());
    }

    @Test
    public void verifyWebApplicationExpire() {
        val testService = RegisteredServiceTestUtils.getService(TGT_ID);
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        t.grantServiceTicket(ID_GENERATOR
                .getNewTicketId(ServiceTicket.PREFIX), testService,
            NeverExpiresExpirationPolicy.INSTANCE, false, true);
        assertFalse(t.isExpired());
        t.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepMostRecentSession() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceTicketKeepMostRecentSession() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com?test"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com;JSESSIONID=xxx"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceWithPathTicketKeepMostRecentSession() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com/webapp1?test=true"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepAll() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            false);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceTicket() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService2(),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceOnPathTicket() {
        val t = new TicketGrantingTicketImpl(TGT_ID, null, null,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);
        t.grantServiceTicket(
            ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
            RegisteredServiceTestUtils.getService("http://host.com/webapp2"),
            NeverExpiresExpirationPolicy.INSTANCE,
            false,
            true);

        assertEquals(2, t.getServices().size());
    }
}
