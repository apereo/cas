package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;

import java.util.Collection;

/**
 * @deprecated As of 4.2.
 * Strategy interface to denote the start of cleaning the registry.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Deprecated
public interface RegistryCleaner {

    /**
     * Method to kick-off the cleaning of a registry.
     * @return the collection of removed/cleaned tickets
     */
    Collection<Ticket> clean();
}
