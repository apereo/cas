/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.remoting.server;

import com.google.common.base.Predicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.validation.Assertion;
import org.springframework.util.Assert;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Wrapper implementation around a CentralAuthenticationService that
 * completes the marshalling of parameters from the web-service layer to the
 * service layer. Typically the only thing that is done is to validate the
 * parameters (as you would in the web tier) and then delegate to the service
 * layer.
 * <p>
 * The following properties are required:
 * </p>
 * <ul>
 * <li>centralAuthenticationService - the service layer we are delegating to.</li>
 * </ul>
 *
 * @author Scott Battaglia
   @deprecated As of 4.1. No longer required. The default implementation can be used
   to delegate calls to the service layer from WS.
 * @since 3.0.0
 */
@Deprecated
public final class RemoteCentralAuthenticationService implements CentralAuthenticationService {

    /** The CORE to delegate to. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** The validators to check the Credential. */
    @NotNull
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the Credentials are null or if given
     * invalid credentials.
     */
    @Override
    public TicketGrantingTicket createTicketGrantingTicket(final Credential... credentials)
            throws AuthenticationException, TicketException {

        Assert.notNull(credentials, "credentials cannot be null");
        checkForErrors(credentials);

        return this.centralAuthenticationService.createTicketGrantingTicket(credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceTicket grantServiceTicket(final String ticketGrantingTicketId, final Service service)
            throws TicketException {
        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Ticket> getTickets(@NotNull final Predicate predicate) {
        return this.centralAuthenticationService.getTickets(predicate);
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if given invalid credentials
     */
    @Override
    public ServiceTicket grantServiceTicket(
            final String ticketGrantingTicketId, final Service service, final Credential... credentials)
            throws AuthenticationException, TicketException {

        checkForErrors(credentials);

        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service, credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz)
            throws InvalidTicketException {
        return this.centralAuthenticationService.getTicket(ticketId, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws TicketException {
        return this.centralAuthenticationService.validateServiceTicket(serviceTicketId, service);
    }

    /**
     * {@inheritDoc}
     * <p>Destroy a TicketGrantingTicket and perform back channel logout. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed. May throw an
     * {@link IllegalArgumentException} if the TicketGrantingTicket ID is null.
     *
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     * @return the logout requests.
     */
    @Override
    public List<LogoutRequest> destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        return this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the credentials are invalid.
     */
    @Override
    public TicketGrantingTicket delegateTicketGrantingTicket(final String serviceTicketId, final Credential... credentials)
            throws AuthenticationException, TicketException {

        checkForErrors(credentials);

        return this.centralAuthenticationService.delegateTicketGrantingTicket(serviceTicketId, credentials);
    }

    /**
     * Check for errors by asking the validator to review each credential.
     *
     * @param credentials the credentials
     */
    private void checkForErrors(final Credential... credentials) {
        if (credentials == null) {
            return;
        }

        for (final Credential c : credentials) {
            final Set<ConstraintViolation<Credential>> errors = this.validator.validate(c);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Error validating credentials: " + errors.toString());
            }
        }
    }

    /**
     * Set the CentralAuthenticationService.
     *
     * @param centralAuthenticationService The CentralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Set the list of validators.
     *
     * @param validator The array of validators to use.
     */
    public void setValidator(final Validator validator) {
        this.validator = validator;
    }
}
