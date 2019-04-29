package org.apereo.cas.mock;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

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
@Getter
@EqualsAndHashCode(of = {"id"})
public class MockTicketGrantingTicket implements TicketGrantingTicket, TicketState {

    public static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private static final long serialVersionUID = 6546995681334670659L;

    private final String id;

    private final Authentication authentication;

    private final ZonedDateTime created;
    private final Map<String, Service> services = new HashMap<>();
    private final Map<String, Service> proxyGrantingTickets = new HashMap<>();
    private int usageCount;
    private boolean expired;

    @Setter
    private ExpirationPolicy expirationPolicy = new TicketGrantingTicketExpirationPolicy(100, 100);

    public MockTicketGrantingTicket(final String principal, final Credential c, final Map<String, List<Object>> attributes) {
        id = ID_GENERATOR.getNewTicketId("TGT");
        val metaData = new BasicCredentialMetaData(c);
        authentication = new DefaultAuthenticationBuilder(new DefaultPrincipalFactory().createPrincipal(principal, attributes))
            .addCredential(metaData)
            .addSuccess(SimpleTestUsernamePasswordAuthenticationHandler.class.getName(),
                new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(), metaData)).build();
        created = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public MockTicketGrantingTicket(final String principal) {
        this(principal, new HashMap());
    }

    public MockTicketGrantingTicket(final String principal, final Map attributes) {
        this(principal, CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("uid", "password"), attributes);
    }

    @Override
    public void update() {
        usageCount++;
    }

    public ServiceTicket grantServiceTicket(final Service service) {
        return grantServiceTicket(ID_GENERATOR.getNewTicketId("ST"), service, null,
            false, true);
    }

    @Override
    public ServiceTicket grantServiceTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy,
                                            final boolean credentialProvided, final boolean onlyTrackMostRecentSession) {
        update();
        return new MockServiceTicket(id, service, this, expirationPolicy);
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
    public TicketGrantingTicket getTicketGrantingTicket() {
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
        return this.expirationPolicy;
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
    public String getPrefix() {
        return TicketGrantingTicket.PREFIX;
    }
}
