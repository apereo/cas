/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

/**
 * Class to maintain constant names for views.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class ViewNames {

    /** View for if the creation of a "Proxy" Ticket Fails */
    public static final String CONST_PROXY_FAILURE = "casProxyFailureView";

    /** View for if the creation of a "Proxy" Ticket Succeeds */
    public static final String CONST_PROXY_SUCCESS = "casProxySuccessView";

    /** View for logging out of CAS */
    public static final String CONST_LOGOUT = "casLogoutView";

    /** View for confirming logon (warn) of CAS */
    public static final String CONST_LOGON_CONFIRM = "casLogonConfirmView";

    /** View for showing the logon form for CAS */
    public static final String CONST_LOGON = "casLogonView";

    /** Generic Success View for CAS if there is no service */
    public static final String CONST_LOGON_SUCCESS = "casLogonGenericSuccessView";

    /** View if Service Ticket Validation Fails */
    public static final String CONST_SERVICE_FAILURE = "casServiceFailureView";

    /** View if Service Ticket Validation Succeeds */
    public static final String CONST_SERVICE_SUCCESS = "casServiceSuccessView";
}