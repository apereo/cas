/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.ticket.registry;

/**
 * Interface to start the cleaning of a registry.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface RegistryCleaner {

    /**
     * Method to kick-off the cleaning of a registry.
     */
    void clean();
}