/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * A monitor observes a resource and reports its status.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public interface Monitor<S extends Status> {

    /**
     * Gets the name of the monitor.
     *
     * @return Monitor name.
     */
    String getName();


    /**
     * Observes the monitored resource and reports the status.
     *
     * @return Status of monitored resource.
     */
    S observe();
}
