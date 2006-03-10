/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Marker interface for Services. Services are generally either remote
 * applications utilizing CAS or applications that principals wish to gain
 * access to. In most cases this will be some form of web application.
 * 
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>This is a published and supported CAS Server 3 API.</p>
 */
public interface Service extends Principal {
    // marker interface for Services
}
