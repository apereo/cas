/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.domain;

/**
 * Interface representing a service that is approved to use CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface RegisteredService {

    /**
     * Is this service enabled/disabled (usually temporary).
     * 
     * @return true if the service is enabled, false if it is temporarily
     * disabled.
     */
    boolean isEnabled();

    /**
     * Return a unique string identifier for this service.
     * 
     * @return The identifier. Never null.
     */
    String getId();

    /**
     * More detailed description of the service.
     * 
     * @return the name of the service.
     */
    String getName();

    /**
     * Return the service url for this service.
     * 
     * @return the service url. Never null.
     */
    String getUrl();

    /**
     * Is this service allowed to proxy?
     * 
     * @return true if it is, false otherwise.
     */
    boolean isAllowedToProxy();

    /**
     * Does this service participate in the single sign on session?
     * 
     * @return true if it does, false otherwise.
     */
    boolean isSsoParticipant();

    /**
     * @return
     */
    String[] getAllowedAttributeNames();
}
