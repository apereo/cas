/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.radius.authentication.handler.support;

import org.jasig.cas.adaptors.radius.JRadiusServerImpl;
import org.jasig.cas.adaptors.radius.RadiusServer;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.Assert;

/**
 * Authentication Handler to authenticate a user against a RADIUS server.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public final class RadiusAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {
    
    /** Array of RADIUS servers to authenticate against. */
    private RadiusServer[] servers;
    
    /** Determines whether to fail over to the next configured RadiusServer if there was an exception. */
    private boolean failoverOnException;
    
    /** Determines whether to fail over to the next configured RadiusServer if there was an authentication failure. */
    private boolean failoverOnAuthenticationFailure;

    protected boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) throws AuthenticationException {
        
        for (int i = 0; i < this.servers.length; i++) {
            try {
                final boolean response = this.servers[i].authenticate(credentials);
                
                if (response || (!response && !this.failoverOnAuthenticationFailure)) {
                    return response;
                }
                
                log.debug("Failing over to next handler because failoverOnAuthenticationFailure is set to true.");
            } catch (Exception e) {
              if (!this.failoverOnException) {
                  log.warn("Failover disabled.  Returning false for authentication request.");
              } else {
                  log.warn("Failover enabled.  Trying next RadiusServer.");
              }
            }
        }

        return false;
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if there was an authentication failure.
     * 
     * @param failoverOnAuthenticationFailure boolean on whether to failover or not.
     */
    public void setFailoverOnAuthenticationFailure(
        final boolean failoverOnAuthenticationFailure) {
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if there was an exception.
     * 
     * @param failoverOnException boolean on whether to failover or not.
     */
    public void setFailoverOnException(final boolean failoverOnException) {
        this.failoverOnException = failoverOnException;
    }

    public void setServers(final JRadiusServerImpl[] servers) {
        this.servers = servers;
    }

    protected void afterPropertiesSetInternal() throws Exception {
        Assert.notEmpty(this.servers, "servers cannot be empty.");
    }    
}
