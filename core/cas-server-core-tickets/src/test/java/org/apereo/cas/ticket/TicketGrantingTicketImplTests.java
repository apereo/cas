package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
class TicketGrantingTicketImplTests {

    private static final File TGT_JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "tgt.json");

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        MAPPER.findAndRegisterModules();
    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
    class TrackingAllowed extends BaseTicketFactoryTests {
        @Test
        void verifyServiceTicketAsFromInitialCredentials() throws Throwable {
            val tgt = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            val serviceTicket = (RenewableServiceTicket) tgt.grantServiceTicket(ID_GENERATOR
                    .getNewTicketId(ServiceTicket.PREFIX), RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
            assertTrue(serviceTicket.isFromNewLogin());
        }

        @Test
        void verifyServiceTicketAsFromNotInitialCredentials() throws Throwable {
            val tgt = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            tgt.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            val serviceTicket = (RenewableServiceTicket) tgt.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            assertFalse(serviceTicket.isFromNewLogin());
        }

        @Test
        void verifyWebApplicationServices() throws Throwable {
            val id = UUID.randomUUID().toString();
            val testService = RegisteredServiceTestUtils.getService(id);
            val t = new TicketGrantingTicketImpl(id, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            t.grantServiceTicket(ID_GENERATOR
                    .getNewTicketId(ServiceTicket.PREFIX), testService,
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
            val services = t.getServices();
            assertEquals(1, services.size());
            val ticketId = services.keySet().iterator().next();
            assertEquals(testService, services.get(ticketId));
            t.removeAllServices();
            val services2 = t.getServices();
            assertEquals(0, services2.size());
        }

        @Test
        void verifyWebApplicationExpire() throws Throwable {
            val id = UUID.randomUUID().toString();
            val testService = RegisteredServiceTestUtils.getService(id);
            val t = new TicketGrantingTicketImpl(id, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            t.grantServiceTicket(ID_GENERATOR
                    .getNewTicketId(ServiceTicket.PREFIX), testService,
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
            assertFalse(t.isExpired());
            t.markTicketExpired();
            assertTrue(t.isExpired());
        }

        @Test
        void verifyDoubleGrantSameServiceTicketKeepMostRecentSession() throws Throwable {
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);

            assertEquals(1, t.getServices().size());
        }

        @Test
        void verifyDoubleGrantSimilarServiceTicketKeepMostRecentSession() throws Throwable {
            val id = UUID.randomUUID().toString();
            val t = new TicketGrantingTicketImpl(id, null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com?test"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com;JSESSIONID=xxx"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);

            assertEquals(1, t.getServices().size());
        }

        @Test
        void verifyDoubleGrantSimilarServiceWithPathTicketKeepMostRecentSession() throws Throwable {
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1?test=true"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);

            assertEquals(1, t.getServices().size());
        }

        @Test
        void verifyDoubleGrantDifferentServiceTicket() throws Throwable {
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            t.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService2(),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);

            assertEquals(2, t.getServices().size());
        }

        @Test
        void verifyDoubleGrantDifferentServiceOnPathTicket() throws Throwable {
            val tgt = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            tgt.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp1"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);
            tgt.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                RegisteredServiceTestUtils.getService("http://host.com/webapp2"),
                NeverExpiresExpirationPolicy.INSTANCE,
                false,
                serviceTicketSessionTrackingPolicy);

            assertEquals(2, tgt.getServices().size());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=false")
    class TrackingDisabled extends BaseTicketFactoryTests {
        @Test
        void verifyDoubleGrantSameServiceTicketKeepAll() throws Throwable {
            val tgt = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            for (var i = 0; i < 5; i++) {
                tgt.grantServiceTicket(
                    ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                    RegisteredServiceTestUtils.getService(),
                    NeverExpiresExpirationPolicy.INSTANCE,
                    false,
                    serviceTicketSessionTrackingPolicy);
            }
            assertEquals(5, tgt.getServices().size());
        }
    }

    @Nested
    class DefaultTests {
        @Test
        void verifySerializeToJson() throws IOException {
            val authenticationWritten = CoreAuthenticationTestUtils.getAuthentication();
            val expirationPolicyWritten = NeverExpiresExpirationPolicy.INSTANCE;
            val tgtWritten = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                authenticationWritten, expirationPolicyWritten);

            MAPPER.writeValue(TGT_JSON_FILE, tgtWritten);
            val tgtRead = MAPPER.readValue(TGT_JSON_FILE, TicketGrantingTicketImpl.class);
            assertEquals(tgtWritten, tgtRead);
            assertEquals(authenticationWritten, tgtRead.getAuthentication());
        }

        @Test
        void verifyEquals() {
            val tgt = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            assertNotNull(tgt);
            assertNotEquals(new Object(), tgt);
            assertEquals(tgt, tgt);
        }

        @Test
        void verifyNullAuthentication() {
            assertThrows(Exception.class, () -> new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null, null, NeverExpiresExpirationPolicy.INSTANCE));
        }

        @Test
        void verifyGetAuthentication() {
            val authentication = CoreAuthenticationTestUtils.getAuthentication();
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null, authentication, NeverExpiresExpirationPolicy.INSTANCE);
            assertEquals(t.getAuthentication(), authentication);
            assertEquals(t.getId(), t.toString());
        }

        @Test
        void verifyIsRootTrue() {
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            assertTrue(t.isRoot());
        }

        @Test
        void verifyIsRootFalse() {
            val t1 = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getService("gantor"), t1,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            assertFalse(t.isRoot());
        }

        @Test
        void verifyProperRootIsReturned() {
            val t1 = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            val t2 = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getService("gantor"), t1,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            val t3 = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getService("gantor"), t2,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);

            assertSame(t1, t3.getRoot());
        }

        @Test
        void verifyGetChainedPrincipalsWithOne() {
            val authentication = CoreAuthenticationTestUtils.getAuthentication();
            val principals = new ArrayList<Authentication>();
            principals.add(authentication);

            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                authentication, NeverExpiresExpirationPolicy.INSTANCE);

            assertEquals(principals, t.getChainedAuthentications());
        }

        @Test
        void verifyCheckCreationTime() {
            val authentication = CoreAuthenticationTestUtils.getAuthentication();

            val startTime = ZonedDateTime.now(ZoneOffset.UTC).minusNanos(100);
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                authentication, NeverExpiresExpirationPolicy.INSTANCE);
            val finishTime = ZonedDateTime.now(ZoneOffset.UTC).plusNanos(100);
            assertTrue(startTime.isBefore(t.getCreationTime()) && finishTime.isAfter(t.getCreationTime()));
        }

        @Test
        void verifyGetChainedPrincipalsWithTwo() {
            val authentication = CoreAuthenticationTestUtils.getAuthentication();
            val authentication1 = CoreAuthenticationTestUtils.getAuthentication("test1");
            val principals = new ArrayList<Authentication>();
            principals.add(authentication);
            principals.add(authentication1);

            val t1 = new TicketGrantingTicketImpl(UUID.randomUUID().toString(), null, null,
                authentication1, NeverExpiresExpirationPolicy.INSTANCE);
            val t = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getService("gantor"), t1,
                authentication, NeverExpiresExpirationPolicy.INSTANCE);

            assertEquals(principals, t.getChainedAuthentications());
        }
    }

}
