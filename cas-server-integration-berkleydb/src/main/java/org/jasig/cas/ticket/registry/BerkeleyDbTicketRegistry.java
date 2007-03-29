package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.springframework.beans.factory.DisposableBean;
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
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * Implementation of the TicketRegistry that is backed by a berkely Db.
 * 
 */
public class BerkeleyDbTicketRegistry extends AbstractDistributedTicketRegistry implements DisposableBean {

	private static final Log log = LogFactory
			.getLog(BerkeleyDbTicketRegistry.class);

	EntryBinding tgtBinding;

	EntryBinding stBinding;

	EntryBinding ticketBinding;

	Database ticketDb;

	Environment registryEnv;

	Database catalogDb;

	TransactionConfig txnConfig = new TransactionConfig();

	{
		txnConfig.setReadCommitted(true);
	}

	Resource dbHome = new FileSystemResource(".");

	public BerkeleyDbTicketRegistry() {

	}

	public void init() {

		/* Create a new, transactional database environment */
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		envConfig.setTxnNoSync(true);
		envConfig.setTxnWriteNoSync(true);
		/* Make a database within that environment */
		Transaction txn = null;
		try {
			registryEnv = new Environment(dbHome.getFile(), envConfig);
			txn = registryEnv.beginTransaction(null, null);
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setTransactional(true);
			dbConfig.setAllowCreate(true);
			ticketDb = registryEnv.openDatabase(txn, "ticketDb", dbConfig);

			DatabaseConfig catalogConfig = new DatabaseConfig();
			catalogConfig.setTransactional(true);
			catalogConfig.setAllowCreate(true);

			catalogDb = registryEnv.openDatabase(txn, "catalogDb",
					catalogConfig);
			StoredClassCatalog catalog = new StoredClassCatalog(catalogDb);

			ticketBinding = new SerialBinding(catalog, Ticket.class);

			txn.commit();
		} catch (Exception e) {
			log.error(e,e);
			abortTransaction(txn);
			throw new RuntimeException(
					"Ticket Registry DB failed to initialize", e);
		}

	}

	/**
	 * @throws IllegalArgumentException
	 *             if the Ticket is null.
	 */
	public void addTicket(final Ticket ticket) {
		Assert.notNull(ticket, "ticket cannot be null");

		if (log.isDebugEnabled()) {
			log.debug("Added ticket [" + ticket.getId() + "] to registry.");
		}

		/* DatabaseEntry represents the key and data of each record */
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		Transaction txn = null;
		try {
			StringBinding.stringToEntry(ticket.getId(), keyEntry);
			ticketBinding.objectToEntry(ticket, dataEntry);

			txn = registryEnv.beginTransaction(null, txnConfig);
			OperationStatus status = ticketDb.put(txn, keyEntry, dataEntry);

			if (status != OperationStatus.SUCCESS) {
				throw new DatabaseException("Data insertion got status "
						+ status);
			}
			txn.commit();
		} catch (DatabaseException e) {
			abortTransaction(txn);
			log.error(e,e);
			throw new RuntimeException(
					"Ticket Registry DB failed to add ticket : " + ticket, e);
		}

	}

	public Ticket getTicket(final String ticketId) {
		if (log.isDebugEnabled()) {
			log.debug("Attempting to retrieve ticket [" + ticketId + "]");
		}

		// Create DatabaseEntry objects for the key and data
		DatabaseEntry theKey = new DatabaseEntry();
		StringBinding.stringToEntry(ticketId, theKey);
		DatabaseEntry theData = new DatabaseEntry();
		Transaction txn = null;
		try {
			txn = registryEnv.beginTransaction(null, txnConfig);
			ticketDb.get(txn, theKey, theData, LockMode.DEFAULT);
			txn.commit();

		} catch (DatabaseException e) {
			abortTransaction(txn);
			log.error(e,e);
			throw new RuntimeException(
					"Ticket Registry DB failed to retrieve ticket : "
							+ ticketId, e);
		}
		Ticket ticket = null;
		if (theData.getData() != null) {
			ticket = (Ticket) ticketBinding.entryToObject(theData);
		}

		if (ticket != null && log.isDebugEnabled()) {
			log.debug("Ticket [" + ticketId + "] found in registry.");
		}

		return getProxiedTicketInstance(ticket);
	}

	public boolean deleteTicket(final String ticketId) {
		if (log.isDebugEnabled()) {
			log.debug("Removing ticket [" + ticketId + "] from registry");
		}

		boolean result = false;
		Transaction txn = null;
		try {
			// Create DatabaseEntry objects for the key and data
			DatabaseEntry theKey = new DatabaseEntry();
			StringBinding.stringToEntry(ticketId, theKey);

			// Perform the deletion. All records that use this key are
			// deleted.
			txn = registryEnv.beginTransaction(null, txnConfig);
			OperationStatus status = ticketDb.delete(txn, theKey);
			if (status == OperationStatus.SUCCESS) {
				result = true;
			}
			txn.commit();
		} catch (DatabaseException e) {
			abortTransaction(txn);
			log.error(e,e);
			throw new RuntimeException(
					"Ticket Registry DB failed to delete ticket : " + ticketId, e);
		}
		return result;
	}

	public Collection getTickets() {
		Cursor cursor = null;
		List<Ticket> tickets = new ArrayList<Ticket>();
		Transaction tx = null;
		try {

			// Open the cursor.
			tx = registryEnv.beginTransaction(null, txnConfig);

			CursorConfig cconfig = new CursorConfig();
			cconfig.setReadUncommitted(true);
			cursor = ticketDb.openCursor(tx, cconfig);

			// Cursors need a pair of DatabaseEntry objects to operate.
			// These
			// hold
			// the key and data found at any given position in the database.
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();

			while (cursor.getNext(foundKey, foundData,
					LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {

				if (foundData.getData() != null) {
					Ticket ticket = (Ticket) ticketBinding
							.entryToObject(foundData);
					tickets.add(getProxiedTicketInstance(ticket));
				}
			}
			cursor.close();
			tx.commit();
		} catch (DatabaseException de) {
			try{
				cursor.close();
			}catch(DatabaseException e){
				log.error(e,e);
			}
			abortTransaction(tx);
			log.error(de, de);
			throw new RuntimeException(
					"Ticket Registry DB failed to get tickets", de);

		}
		return tickets;
	}

	/**
	 * @return the dbHome
	 */
	public Resource getDbHome() {
		return dbHome;
	}

	/**
	 * @param dbHome
	 *            the dbHome to set
	 */
	public void setDbHome(Resource dbHome) {
		this.dbHome = dbHome;
	}

	public void destroy() throws Exception {
        catalogDb.close();
        ticketDb.close();
        registryEnv.close();
        
        // TODO handle errors
        
    }

	protected void updateTicket(Ticket ticket) {
		addTicket(ticket);
	}
	
	private void abortTransaction(Transaction transaction){
		if(transaction != null){
			try {
				transaction.abort();
			} catch (DatabaseException e) {
				log.error(e,e);
			}
		}
	}
}
