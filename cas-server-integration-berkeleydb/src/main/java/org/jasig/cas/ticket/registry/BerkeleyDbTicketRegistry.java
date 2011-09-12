/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jasig.cas.ticket.Ticket;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import javax.validation.constraints.NotNull;

/**
 * Implementation of the TicketRegistry that is backed by a BerkeleyDb.
 * 
 * @author Andres March
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class BerkeleyDbTicketRegistry extends
    AbstractDistributedTicketRegistry implements InitializingBean,
    DisposableBean {

    private EntryBinding ticketBinding;

    private Database ticketDb;

    private Database catalogDb;

    private Environment environment;

    @NotNull
    private Resource dbHome = new FileSystemResource(".");

    public BerkeleyDbTicketRegistry() {
        // nothing to do
    }

    public void afterPropertiesSet() throws Exception {
        final EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        envConfig.setTxnNoSync(true);
        envConfig.setTxnWriteNoSync(true);

        final DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        this.environment = new Environment(this.dbHome.getFile(), envConfig);
        this.ticketDb = this.environment.openDatabase(null, "ticketDb",
            dbConfig);
        this.catalogDb = this.environment.openDatabase(null, "catalogDb",
            dbConfig);
        StoredClassCatalog catalog = new StoredClassCatalog(this.catalogDb);
        this.ticketBinding = new SerialBinding(catalog, Ticket.class);
    }

    /**
     * @throws IllegalArgumentException if the Ticket is null.
     */
    public void addTicket(final Ticket ticket) {
        Assert.notNull(ticket, "ticket cannot be null");

        if (log.isDebugEnabled()) {
            log.debug("Added ticket [" + ticket.getId() + "] to registry.");
        }

        final DatabaseEntry dataEntry = new DatabaseEntry();
        try {
            this.ticketBinding.objectToEntry(ticket, dataEntry);

            final OperationStatus status = this.ticketDb.put(null,
                getKeyFromString(ticket.getId()), dataEntry);

            if (status != OperationStatus.SUCCESS) {
                throw new DatabaseException("Data insertion got status "
                    + status);
            }
        } catch (final DatabaseException e) {
            throw new RuntimeException(
                "Ticket Registry DB failed to add ticket : " + ticket, e);
        }

    }

    public Ticket getTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve ticket [" + ticketId + "]");
        }

        final DatabaseEntry theData = new DatabaseEntry();
        try {
            this.ticketDb.get(null, getKeyFromString(ticketId), theData,
                LockMode.DEFAULT);

        } catch (final DatabaseException e) {
            throw new RuntimeException(e);
        }

        final Ticket ticket = theData.getData() != null
            ? (Ticket) this.ticketBinding.entryToObject(theData) : null;

        if (ticket != null && log.isDebugEnabled()) {
            log.debug("Ticket [" + ticketId + "] found in registry.");
        }

        return getProxiedTicketInstance(ticket);
    }

    public boolean deleteTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketId + "] from registry");
        }

        try {
            return this.ticketDb.delete(null, getKeyFromString(ticketId)) == OperationStatus.SUCCESS;
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Collection<Ticket> getTickets() {
        Cursor cursor = null;
        final List<Ticket> tickets = new ArrayList<Ticket>();
        
        try {
            final CursorConfig cconfig = new CursorConfig();
            cconfig.setReadUncommitted(true);
            cursor = this.ticketDb.openCursor(null, cconfig);

            /*
             * Cursors need a pair of DatabaseEntry objects to operate. These
             * hold the key and data found at any given position in the
             * database.
             */
            final DatabaseEntry foundKey = new DatabaseEntry();
            final DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData,
                LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {

                if (foundData.getData() != null) {
                    Ticket ticket = (Ticket) this.ticketBinding
                        .entryToObject(foundData);
                    tickets.add(getProxiedTicketInstance(ticket));
                }
            }
            cursor.close();
        } catch (DatabaseException de) {
            try {
                if (cursor != null) {
                    cursor.close(); 
                }
            } catch (DatabaseException e) {
                // nothing to do
            }
            throw new RuntimeException(de);

        }
        return tickets;
    }

    /**
     * @param dbHome the dbHome to set
     */
    public void setDbHome(final Resource dbHome) {
        this.dbHome = dbHome;
    }

    public void destroy() throws Exception {
        this.catalogDb.close();
        this.ticketDb.close();
        this.environment.close();
    }

    protected void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    private DatabaseEntry getKeyFromString(final String key) {
        final DatabaseEntry de = new DatabaseEntry();
        StringBinding.stringToEntry(key, de);

        return de;
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }
}
