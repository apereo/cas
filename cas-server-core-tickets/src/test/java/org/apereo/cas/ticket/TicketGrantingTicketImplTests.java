package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockService;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import org.junit.Assert;
import org.junit.Test;

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

    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    @Test
    public void verifyEquals() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertNotNull(t);
        assertFalse(t.equals(new Object()));
        assertTrue(t.equals(t));
    }

    @Test(expected=Exception.class)
    public void verifyNullAuthentication() {
        new TicketGrantingTicketImpl("test", null, null, null,
                new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyGetAuthentication() {
        final Authentication authentication = TestUtils.getAuthentication();

        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            authentication, new NeverExpiresExpirationPolicy());

        Assert.assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    @Test
    public void verifyIsRootTrue() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    @Test
    public void verifyIsRootFalse() {
        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getService("gantor"), t1,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    @Test
    public void verifyGetChainedPrincipalsWithOne() {
        final Authentication authentication = TestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);

        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyCheckCreationTime() {
        final Authentication authentication = TestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);

        final ZonedDateTime startTime = ZonedDateTime.now(ZoneOffset.UTC).minusNanos(100);
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            authentication, new NeverExpiresExpirationPolicy());
        final ZonedDateTime finishTime = ZonedDateTime.now(ZoneOffset.UTC).plusNanos(100);
        assertTrue(startTime.isBefore(t.getCreationTime()) && finishTime.isAfter(t.getCreationTime()));
    }

    @Test
    public void verifyGetChainedPrincipalsWithTwo() {
        final Authentication authentication = TestUtils.getAuthentication();
        final Authentication authentication1 = TestUtils.getAuthentication("test1");
        final List<Authentication> principals = new ArrayList<>();
        principals.add(authentication);
        principals.add(authentication1);

        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null, null,
            authentication1, new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getService("gantor"), t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void verifyServiceTicketAsFromInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), org.apereo.cas.services.TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false, true);

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyServiceTicketAsFromNotInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        final ServiceTicket s = t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyWebApplicationServices() {
        final MockService testService = new MockService("test");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
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
        final MockService testService = new MockService("test");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
                        .getNewTicketId(ServiceTicket.PREFIX), testService,
                new NeverExpiresExpirationPolicy(), false, true);
        assertFalse(t.isExpired());
        t.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com?test"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com;JSESSIONID=xxx"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSimilarServiceWithPathTicketKeepMostRecentSession() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com/webapp1"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com/webapp1?test=true"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(1, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantSameServiceTicketKeepAll() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                false);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceTicket() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService2(),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(2, t.getServices().size());
    }

    @Test
    public void verifyDoubleGrantDifferentServiceOnPathTicket() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
                TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com/webapp1"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                org.apereo.cas.services.TestUtils.getService("http://host.com/webapp2"),
                new NeverExpiresExpirationPolicy(),
                false,
                true);

        assertEquals(2, t.getServices().size());
    }
}
