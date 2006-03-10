/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.Serializable;

/**
 * Marker interface for credentials required to authenticate a principal.
 * <p>
 * The Credentials is an opaque object that represents the information a user
 * asserts proves that the user is who it says it is. In CAS, any information
 * that is to be presented for authentication must be wrapped (or implement) the
 * Credentials interface. Credentials can contain a userid and password, or a
 * Certificate, or an IP address, or a cookie value. Some credentials require
 * validation, while others (such as container based or Filter based validation)
 * are inherently trustworthy.
 * 
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>This is a published and supported CAS Server 3 API.</p>
 */
public interface Credentials extends Serializable {
    // marker interface contains no methods
}
