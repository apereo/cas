package org.jasig.cas.ticket.registry.support;

import org.apache.commons.collections.CollectionUtils;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.BatchableTicketRegistry;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * This class tries to address the issue with ${@link DefaultTicketRegistryCleaner} when used in conjunction with a
 * ${@link org.jasig.cas.ticket.registry.JpaTicketRegistryy} discussed in the below thread by processing the tickets in
 * the registry in batches. This should prevent the CAS server from running out of memory in certain scenarios (ie when
 * a large number of tickets get created in a short period of time) when the ticket registry cleaner runs.
 *
 * http://jasig.275507.n4.nabble.com/JpaTicketRegistry-A-Sinking-Ship-td4256973.html
 *
 * @author Ahsan Rabbani
 */
public class BatchedTicketRegistryCleaner implements RegistryCleaner
{
    /** The Commons Logging instance. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_BATCH_SIZE = 2000;

    /** The instance of the TicketRegistry to clean. */
    @NotNull
    private BatchableTicketRegistry ticketRegistry;

    /** Execution locking strategy */
    @NotNull
    private LockingStrategy lock = new NoOpLockingStrategy();

    private TicketRegistryCleanerHelper registryCleanerHelper = new TicketRegistryCleanerHelper();

    private boolean logUserOutOfServices = true;

    private int batchSize = DEFAULT_BATCH_SIZE;

    @Override
    public void clean()
    {
        this.log.info("Beginning ticket cleanup.");

        this.log.debug("Attempting to acquire ticket cleanup lock.");
        if (!this.lock.acquire())
        {
            this.log.info("Could not obtain lock.  Aborting cleanup.");
            return;
        }
        this.log.debug("Acquired lock.  Proceeding with cleanup.");

        try
        {
            this.log.info("Processing TicketGrantingTickets for cleanup.");
            int numTicketsRemoved = cleanTickets(
                    new BatchedTicketRetriever()
                    {
                        @Override
                        public Collection<Ticket> getBatch(int offset, int batchSize)
                        {
                            return ticketRegistry.getTicketGrantingTicketBatch(offset, batchSize);
                        }
                    }
            );
            this.log.info("{} total TicketGrantingTickets removed.", numTicketsRemoved);

            this.log.info("Processing ServiceTickets for cleanup.");
            numTicketsRemoved = cleanTickets(
                    new BatchedTicketRetriever()
                    {
                        @Override
                        public Collection<Ticket> getBatch(int offset, int batchSize)
                        {
                            return ticketRegistry.getServiceTicketBatch(offset, batchSize);
                        }
                    }
            );
            this.log.info("{} total ServiceTickets removed.", numTicketsRemoved);
        }
        finally
        {
            this.log.debug("Releasing ticket cleanup lock.");
            this.lock.release();
        }

        this.log.info("Finished ticket cleanup.");
    }

    int cleanTickets(BatchedTicketRetriever ticketRetriever)
    {
        int offset = 0;
        int numTicketsRemoved = 0;

        Collection<Ticket> ticketBatch;
        while (CollectionUtils.isNotEmpty(ticketBatch = ticketRetriever.getBatch(offset, batchSize)))
        {
            int ticketsRemovedFromBatch = registryCleanerHelper.deleteExpiredTickets(ticketRegistry, ticketBatch, logUserOutOfServices);
            offset += (ticketBatch.size() - ticketsRemovedFromBatch);
            numTicketsRemoved += ticketsRemovedFromBatch;
        }

        return numTicketsRemoved;
    }

    interface BatchedTicketRetriever
    {
        Collection<Ticket> getBatch(int offset, int batchSize);
    }

    /**
     * @param ticketRegistry The  ticketRegistry to set.
     */
    public void setTicketRegistry(final BatchableTicketRegistry ticketRegistry)
    {
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * @param  strategy  Ticket cleanup locking strategy.  An exclusive locking
     * strategy is preferable if not required for some ticket backing stores,
     * such as JPA, in a clustered CAS environment.  Use {@link JdbcLockingStrategy}
     * for {@link org.jasig.cas.ticket.registry.JpaTicketRegistry} in a clustered
     * CAS environment.
     */
    public void setLock(final LockingStrategy strategy)
    {
        this.lock = strategy;
    }

    /**
     * Whether to log users out of services when we remove an expired ticket.  The default is true. Set this to
     * false to disable.
     *
     * @param logUserOutOfServices whether to log the user out of services or not.
     */
    public void setLogUserOutOfServices(final boolean logUserOutOfServices)
    {
        this.logUserOutOfServices = logUserOutOfServices;
    }

    /**
     * Batch size to use when retrieving tickets from the registry to process. The default is 2000.
     *
     * @param batchSize batch size to use when retrieving tickets from the registry
     */
    public void setBatchSize(final int batchSize)
    {
        this.batchSize = batchSize;
    }

    void setRegistryCleanerHelper(TicketRegistryCleanerHelper registryCleanerHelper)
    {
        this.registryCleanerHelper = registryCleanerHelper;
    }
}
