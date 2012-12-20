/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.support.RadiusServer;

/**
 * Authentication Handler to authenticate a user against a RADIUS server.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class RadiusAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** Array of RADIUS servers to authenticate against. */
    @NotNull
    @Size(min=1)
    private List<RadiusServer> servers;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     */
    private boolean failoverOnException;

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an authentication failure.
     */
    private boolean failoverOnAuthenticationFailure;

    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, IOException {

        for (final RadiusServer radiusServer : this.servers) {
            try {
                if (radiusServer.authenticate(credential)) {
                    return new HandlerResult(this, new SimplePrincipal(credential.getUsername()));
                }
                if (!this.failoverOnAuthenticationFailure) {
                    break;
                }
                log.debug("Failing over to next handler because failoverOnAuthenticationFailure=true.");
            } catch (Exception e) {
                log.debug("Authenticating {} failed with error {}", credential.getUsername(), e);
                if (!this.failoverOnException) {
                    throw new IOException("RADIUS authentication failed with error " + e);
                } else {
                    log.info("Trying next RadiusServer because failoverOnException=true.");
                }
            }
        }
        throw new FailedLoginException();
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an authentication failure.
     * 
     * @param failoverOnAuthenticationFailure boolean on whether to failover or
     * not.
     */
    public void setFailoverOnAuthenticationFailure(
        final boolean failoverOnAuthenticationFailure) {
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     * 
     * @param failoverOnException boolean on whether to failover or not.
     */
    public void setFailoverOnException(final boolean failoverOnException) {
        this.failoverOnException = failoverOnException;
    }

    public void setServers(final List<RadiusServer> servers) {
        this.servers = servers;
    }
}
