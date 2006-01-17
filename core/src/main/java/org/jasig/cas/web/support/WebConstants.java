/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

/**
 * Various constants used by CAS web tier components.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class WebConstants {

    /** Identifier for a ticket in the model/request params. */
    public static final String TICKET = "ticket";
    
    /** Identifier for a code in the model. */
    public static final String CODE = "code";

    /** Identifier for a description in the model. */
    public static final String DESC = "description";

    /** Identifier for a pgtIou in the request params. */
    public static final String PGTIOU = "pgtIou";

    /** Identifier for a ProxyGrantingTicket in the model. */
    public static final String PROXY_GRANTING_TICKET = "pgt";

    /** Identifier for a targetService in the request params. */
    public static final String TARGET_SERVICE = "targetService";

    /** Identifier for a service in the request params. */
    public static final String SERVICE = "service";

    /** Identifier for a warning flag in the request params. */
    public static final String WARN = "warn";

    /** Identifier for a renew flag in the model/request params. */
    public static final String RENEW = "renew";

    /** Identifier for a pgtUrl in the request params. */
    public static final String PGTURL = "pgtUrl";

    /** Identifier for a assertion in the model. */
    public static final String ASSERTION = "assertion";

    /** Identifier for the logout url to display on the logout page. */
    public static final String LOGOUT = "url";
}
