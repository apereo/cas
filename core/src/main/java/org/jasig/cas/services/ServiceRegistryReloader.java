/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Strategy interface that is responsible for reloading service registries at
 * runtime.
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ServiceRegistryReloader {

    /**
     * Method to call to reload the service registry.
     */
    void reloadServiceRegistry();
}
