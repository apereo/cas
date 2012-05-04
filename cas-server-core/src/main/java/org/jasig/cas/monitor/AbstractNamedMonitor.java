/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

/**
 * Description of AbstractNamedMonitor.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public abstract class AbstractNamedMonitor<S extends Status> implements Monitor<S> {
    /** Monitor name. */
    protected String name;


    /**
     * @return Monitor name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n Monitor name.
     */
    public void setName(final String n) {
        this.name = n;
    }
}
