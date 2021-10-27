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
package org.jasig.cas.ticket.support;

import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public final class RememberMeDelegatingExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -2735975347698196127L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeDelegatingExpirationPolicy.class);


    @NotNull
    private ExpirationPolicy rememberMeExpirationPolicy;

    @NotNull
    private ExpirationPolicy sessionExpirationPolicy;


    /** No-arg constructor used for serialization purposes. */
    public RememberMeDelegatingExpirationPolicy() {}

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().
                get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);

        if (b == null || b.equals(Boolean.FALSE)) {
            return this.sessionExpirationPolicy.isExpired(ticketState);
        }

        return this.rememberMeExpirationPolicy.isExpired(ticketState);
    }

    public void setRememberMeExpirationPolicy(
        final ExpirationPolicy rememberMeExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
