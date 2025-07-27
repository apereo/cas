package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

/**
 * The {@link DefaultServiceTicketFactory} is responsible for
 * creating {@link ServiceTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultServiceTicketFactory implements ServiceTicketFactory {
    private final ExpirationPolicyBuilder<ServiceTicket> expirationPolicyBuilder;

    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    private final TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    private final CipherExecutor<String, String> cipherExecutor;

    private final UniqueTicketIdGenerator ticketIdGenerator = new HostNameBasedUniqueTicketIdGenerator();

    private final ServicesManager servicesManager;

    @Override
    public <T extends Ticket> T create(final Service service, final Authentication authentication,
                                       final boolean credentialsProvided, final Class<T> clazz) throws Throwable {
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        val ticketId = produceTicketIdentifier(service, null, credentialsProvided);
        val result = new ServiceTicketImpl(ticketId, null, service, credentialsProvided, expirationPolicyToUse).setAuthentication(authentication);
        result.setTenantId(service.getTenant());
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [%s] is of type %s when we were expecting %s".formatted(result, result.getClass(), clazz));
        }
        result.markTicketStateless();
        return (T) result;
    }

    @Override
    public <T extends Ticket> T create(final TicketGrantingTicket ticketGrantingTicket,
                                       final Service service,
                                       final boolean credentialProvided,
                                       final Class<T> clazz) throws Throwable {
        val ticketId = produceTicketIdentifier(service, ticketGrantingTicket, credentialProvided);
        var result = FunctionUtils.doIf(cipherExecutor.isEnabled(), () -> {
            LOGGER.trace("Attempting to encode service ticket [{}]", ticketId);
            val encoded = cipherExecutor.encode(ticketId);
            LOGGER.debug("Encoded service ticket id [{}]", encoded);
            return encoded;
        }, () -> ticketId).get();
        return produceTicket(ticketGrantingTicket, service, credentialProvided, result, clazz);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return ServiceTicket.class;
    }

    protected <T extends Ticket> T produceTicket(final TicketGrantingTicket ticketGrantingTicket,
                                                 final Service service,
                                                 final boolean credentialProvided,
                                                 final String ticketId,
                                                 final Class<T> clazz) {
        val expirationPolicyToUse = determineExpirationPolicyForService(service);
        val result = ticketGrantingTicket.grantServiceTicket(
            ticketId,
            service,
            expirationPolicyToUse,
            credentialProvided,
            serviceTicketSessionTrackingPolicy);
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [%s] is of type %s when we were expecting %s".formatted(result, result.getClass(), clazz));
        }
        return (T) result;
    }

    protected String produceTicketIdentifier(final Service service, final TicketGrantingTicket ticketGrantingTicket,
                                             final boolean credentialProvided) throws Throwable {
        val uniqueTicketIdGenKey = service.getClass().getName();
        var serviceTicketUniqueTicketIdGenerator = (UniqueTicketIdGenerator) null;
        if (uniqueTicketIdGeneratorsForService != null && !uniqueTicketIdGeneratorsForService.isEmpty()) {
            LOGGER.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
            serviceTicketUniqueTicketIdGenerator = uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        }
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = ticketIdGenerator;
            LOGGER.debug("Service ticket id generator not found for [{}]. Using the default generator.", uniqueTicketIdGenKey);
        }

        return serviceTicketUniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX);
    }

    private ExpirationPolicy determineExpirationPolicyForService(final Service service) {
        val registeredService = servicesManager.findServiceBy(service, CasModelRegisteredService.class);
        if (registeredService != null && registeredService.getServiceTicketExpirationPolicy() != null) {
            val policy = registeredService.getServiceTicketExpirationPolicy();
            val count = policy.getNumberOfUses();
            val ttl = policy.getTimeToLive();
            if (count > 0 && StringUtils.isNotBlank(ttl)) {
                return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
                    count, Beans.newDuration(ttl).toSeconds());
            }
        }
        return expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
