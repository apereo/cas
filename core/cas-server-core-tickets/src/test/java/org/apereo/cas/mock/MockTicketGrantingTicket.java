package org.apereo.cas.mock;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock ticket-granting ticket.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockTicketGrantingTicket implements TicketGrantingTicket, TicketState {
    public static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private static final long serialVersionUID = 6546995681334670659L;

    private final String id;

    private final Authentication authentication;

    private final ZonedDateTime created;

    private int usageCount;

    private boolean expired;

    private final Map<String, Service> services = new HashMap<>();

    private final Map<String, Service> proxyGrantingTickets = new HashMap<>();

    public MockTicketGrantingTicket(final String principal, final Credential c, final Map attributes) {
        id = ID_GENERATOR.getNewTicketId("TGT");
        final CredentialMetaData metaData = new BasicCredentialMetaData(c);
        authentication = new DefaultAuthenticationBuilder(
                new DefaultPrincipalFactory().createPrincipal(principal, attributes))
                .addCredential(metaData)
                .addSuccess(SimpleTestUsernamePasswordAuthenticationHandler.class.getName(),
                        new DefaultHandlerResult(new SimpleTestUsernamePasswordAuthenticationHandler(), metaData))
                .build();

        created = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public MockTicketGrantingTicket(final String principal) {
        this(principal, CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("uid", "password"), new HashMap());
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void update() {
        usageCount++;
    }

    public ServiceTicket grantServiceTicket(final Service service) {
        return grantServiceTicket(ID_GENERATOR.getNewTicketId("ST"), service, null, false, true);
    }

    @Override
    public ServiceTicket grantServiceTicket(
            final String id,
            final Service service,
            final ExpirationPolicy expirationPolicy,
            final boolean credentialProvided,
            final boolean onlyTrackMostRecentSession) {
        update();
        return new MockServiceTicket(id, service, this);
    }

    @Override
    public Service getProxiedBy() {
        return null;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public TicketGrantingTicket getRoot() {
        return this;
    }

    @Override
    public List<Authentication> getChainedAuthentications() {
        return new ArrayList<>(0);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return this;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return created;
    }

    @Override
    public int getCountOfUses() {
        return usageCount;
    }

    @Override
    public ZonedDateTime getLastTimeUsed() {
        return created;
    }

    @Override
    public ZonedDateTime getPreviousTimeUsed() {
        return created;
    }

    @Override
    public ExpirationPolicy getExpirationPolicy() {
        return new TicketGrantingTicketExpirationPolicy(100, 100);
    }

    @Override
    public Map<String, Service> getServices() {
        return this.services;
    }

    @Override
    public Map<String, Service> getProxyGrantingTickets() {
        return this.proxyGrantingTickets;
    }

    @Override
    public void removeAllServices() {
    }

    @Override
    public void markTicketExpired() {
        expired = true;
    }


    @Override
    public int compareTo(final Ticket o) {
        return this.id.compareTo(o.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        return compareTo((Ticket) obj) == 0;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String getPrefix() {
        return TicketGrantingTicket.PREFIX;
    }


}
