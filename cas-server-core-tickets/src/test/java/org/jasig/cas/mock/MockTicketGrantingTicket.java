package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock ticket-granting ticket.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockTicketGrantingTicket implements TicketGrantingTicket {
    public static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private static final long serialVersionUID = 6546995681334670659L;

    private final String id;

    private final Authentication authentication;

    private final Date created;

    private int usageCount;

    private boolean expired;

    private Service proxiedBy;

    private final Map<String, Service> services = new HashMap<>();

    public MockTicketGrantingTicket(final String principal) {
        id = ID_GENERATOR.getNewTicketId("TGT");
        final CredentialMetaData metaData = new BasicCredentialMetaData(
                org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword());
        authentication = new DefaultAuthenticationBuilder(new DefaultPrincipalFactory().createPrincipal(principal))
                            .addCredential(metaData)
                            .addSuccess(SimpleTestUsernamePasswordAuthenticationHandler.class.getName(),
                            new DefaultHandlerResult(new SimpleTestUsernamePasswordAuthenticationHandler(), metaData))
                            .build();

        created = new Date();
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    public ServiceTicket grantServiceTicket(final Service service) {
        return grantServiceTicket(ID_GENERATOR.getNewTicketId("ST"), service, null, true, true);
    }

    @Override
    public ServiceTicket grantServiceTicket(
            final String id,
            final Service service,
            final ExpirationPolicy expirationPolicy,
            final boolean credentialsProvided,
            final boolean onlyTrackMostRecentSession) {
        usageCount++;
        return new MockServiceTicket(id, service, this);
    }

    @Override
    public Service getProxiedBy() {
        return this.proxiedBy;
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
    public List<Authentication> getSupplementalAuthentications() {
        return Collections.emptyList();
    }

    @Override
    public List<Authentication> getChainedAuthentications() {
        return Collections.emptyList();
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
    public long getCreationTime() {
        return created.getTime();
    }

    @Override
    public int getCountOfUses() {
        return usageCount;
    }

    @Override
    public Map<String, Service> getServices() {
        return this.services;
    }

    @Override
    public void removeAllServices() {
    }

    @Override
    public void markTicketExpired() {
        expired = true;
    }
}
