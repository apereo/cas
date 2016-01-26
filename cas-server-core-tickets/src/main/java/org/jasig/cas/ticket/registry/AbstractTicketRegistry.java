package org.jasig.cas.ticket.registry;

import org.jasig.cas.CipherExecutor;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.util.CompressionUtils;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public abstract class AbstractTicketRegistry implements TicketRegistry, TicketRegistryState {

    /** The Slf4j logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketDelegators")
    private List<Pair<Class<? extends Ticket>, Constructor<? extends AbstractTicketDelegator>>> ticketDelegators = new ArrayList<>();

    /**
     * Default constructor which registers the appropriate ticket delegators.
     */
    @SuppressWarnings("unchecked")
    public AbstractTicketRegistry() {
        ticketDelegators.add(new Pair(ProxyGrantingTicket.class,
                AbstractTicketDelegator.getDefaultConstructor(ProxyGrantingTicketDelegator.class)));
        ticketDelegators.add(new Pair(TicketGrantingTicket.class,
                AbstractTicketDelegator.getDefaultConstructor(TicketGrantingTicketDelegator.class)));
        ticketDelegators.add(new Pair(ProxyTicket.class,
                AbstractTicketDelegator.getDefaultConstructor(ProxyTicketDelegator.class)));
        ticketDelegators.add(new Pair(ServiceTicket.class,
                AbstractTicketDelegator.getDefaultConstructor(ServiceTicketDelegator.class)));
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if class is null.
     * @throws ClassCastException if class does not match requested ticket
     * class.
     * @return specified ticket from the registry
     */
    @Override
    public final <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");

        final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                + " is of type " + ticket.getClass()
                + " when we were expecting " + clazz);
        }

        return (T) ticket;
    }

    @Override
    public int sessionCount() {
      logger.debug("sessionCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Integer.MIN_VALUE);
      return Integer.MIN_VALUE;
    }

    @Override
    public int serviceTicketCount() {
      logger.debug("serviceTicketCount() operation is not implemented by the ticket registry instance {}. Returning unknown as {}",
                this.getClass().getName(), Integer.MIN_VALUE);
      return Integer.MIN_VALUE;
    }

    @Override
    public final boolean deleteTicket(final String ticketId) {
        if (ticketId == null) {
            return false;
        }

        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return false;
        }

        if (ticket instanceof TicketGrantingTicket) {
            logger.debug("Removing children of ticket [{}] from the registry.", ticket.getId());
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;
            deleteChildren(tgt);

            final Collection<ProxyGrantingTicket> proxyGrantingTickets = tgt.getProxyGrantingTickets();
            for (final ProxyGrantingTicket proxyGrantingTicket : proxyGrantingTickets) {
                logger.debug("Removing proxy-granting ticket [{}]", proxyGrantingTicket.getId());
                deleteTicket(proxyGrantingTicket.getId());
            }

        }
        logger.debug("Removing ticket [{}] from the registry.", ticket);
        return deleteSingleTicket(ticketId);
    }


    /**
     * Delete TGT's service tickets.
     *
     * @param ticket the ticket
     */
    private void deleteChildren(final TicketGrantingTicket ticket) {
        // delete service tickets
        final Map<String, Service> services = ticket.getServices();
        if (services != null && !services.isEmpty()) {
            for (final Map.Entry<String, Service> entry : services.entrySet()) {
                final String ticketId = entry.getKey();
                if (deleteSingleTicket(ticketId)) {
                    logger.debug("Removed ticket [{}]", entry.getKey());
                } else {
                    logger.debug("Unable to remove ticket [{}]", entry.getKey());
                }
            }
        }
    }


    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public boolean deleteSingleTicket(final Ticket ticketId) {
        return deleteSingleTicket(ticketId.getId());
    }

    /**
     * Delete a single ticket instance from the store.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public abstract boolean deleteSingleTicket(final String ticketId);

    /**
     * Update the received ticket.
     *
     * @param ticket the ticket
     */
    protected abstract void updateTicket(Ticket ticket);

    /**
     * Whether or not a callback to the TGT is required when checking for expiration.
     *
     * @return true, if successful
     */
    protected abstract boolean needsCallback();

    /**
     * Gets the proxied ticket instance.
     *
     * @param ticket the ticket
     * @return the proxied ticket instance
     */
    protected final Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        for (final Pair<Class<? extends Ticket>, Constructor<? extends AbstractTicketDelegator>> ticketDelegator: ticketDelegators) {
            final Class<? extends Ticket> clazz = ticketDelegator.getFirst();
            if (clazz.isAssignableFrom(ticket.getClass())) {
                final Constructor<? extends AbstractTicketDelegator> constructor = ticketDelegator.getSecond();
                try {
                    return constructor.newInstance(this, ticket, needsCallback());
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new IllegalStateException("Cannot wrap ticket of type: " + ticket.getClass() + " with a ticket delegator");
    }

    public void setCipherExecutor(final CipherExecutor<byte[], byte[]> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encode ticket id into a SHA-512.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    protected String encodeTicketId(final String ticketId)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }

        return CompressionUtils.sha512Hex(ticketId);
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticket;
        }

        if (ticket == null) {
            return ticket;
        }

        logger.info("Encoding [{}]", ticket);
        final byte[] encodedTicketObject = CompressionUtils.serializeAndEncodeObject(
                this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final Ticket encodedTicket = new EncodedTicket(
                ByteSource.wrap(encodedTicketObject), encodedTicketId);
        logger.info("Created [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket result) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return result;
        }

        if (result == null) {
            return result;
        }

        logger.info("Attempting to decode {}", result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = CompressionUtils.decodeAndSerializeObject(
                encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
        logger.info("Decoded {}",  ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket> decodeTickets(final Collection<Ticket> items) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return items;
        }

        if (items == null || items.isEmpty()) {
            return items;
        }

        final Collection<Ticket> tickets = new HashSet<>(items.size());
        for (final Ticket item : items) {
            final Ticket ticket = decodeTicket(item);
            tickets.add(ticket);
        }
        return tickets;
    }

    @Nullable
    public List<Pair<Class<? extends Ticket>, Constructor<? extends AbstractTicketDelegator>>> getTicketDelegators() {
        return ticketDelegators;
    }

    public void setTicketDelegators(@Nullable final List<Pair<Class<? extends Ticket>, Constructor<? extends AbstractTicketDelegator>>>
                                            ticketDelegators) {
        this.ticketDelegators = ticketDelegators;
    }
}
