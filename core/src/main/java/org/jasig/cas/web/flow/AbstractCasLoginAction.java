/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.springframework.util.Assert;

/**
 * Abstract class that also introduces the dependency to use CAS to generate
 * tickets.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractCasLoginAction extends AbstractLoginAction {

    private CentralAuthenticationService centralAuthenticationService;

    public final void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    protected final CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    protected void initActionInternal() {
        Assert.notNull(this.centralAuthenticationService, "centralAuthenticationService cannot be null");
    }
}
