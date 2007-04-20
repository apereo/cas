/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Generates a unique consistant Id based on the principal, a service, and some
 * algorithm.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 * TODO generate javadoc
 */
public interface PersistentIdGenerator {

    String generate(Principal principal, Service service);
}
