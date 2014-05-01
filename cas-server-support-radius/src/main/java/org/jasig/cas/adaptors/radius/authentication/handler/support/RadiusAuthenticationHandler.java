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
package org.jasig.cas.adaptors.radius.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.util.List;

import org.jasig.cas.adaptors.radius.RadiusServer;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Authentication Handler to authenticate a user against a RADIUS server.
 *
 * @author Scott Battaglia
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

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        for (final RadiusServer radiusServer : this.servers) {
            logger.debug("Attempting to authenticate {} at {}", username, radiusServer);
            try {
                if (radiusServer.authenticate(username, credential.getPassword())) {
                    return createHandlerResult(credential, new SimplePrincipal(username), null);
                } 
                
                if (!this.failoverOnAuthenticationFailure) {
                    throw new FailedLoginException();
                }
                logger.debug("failoverOnAuthenticationFailure enabled -- trying next server");
            } catch (final PreventedException e) {
                if (!this.failoverOnException) {
                    throw e;
                }
                logger.warn("failoverOnException enabled -- trying next server.", e);
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
    public final void setFailoverOnAuthenticationFailure(
            final boolean failoverOnAuthenticationFailure) {
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
    }

    /**
     * Determines whether to fail over to the next configured RadiusServer if
     * there was an exception.
     *
     * @param failoverOnException boolean on whether to failover or not.
     */
    public final void setFailoverOnException(final boolean failoverOnException) {
        this.failoverOnException = failoverOnException;
    }

    public final void setServers(final List<RadiusServer> servers) {
        this.servers = servers;
    }
}
