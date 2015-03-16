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

package org.jasig.cas.web.report.sso;

import java.util.Collection;
import java.util.Map;

/**
 * An API to provide an aggregate view of CAS <i>live</i> SSO sessions at run time i.e. a collection of
 * un-expired {@link org.jasig.cas.ticket.TicketGrantingTicket} metadata and
 * their associated {@link org.jasig.cas.authentication.Authentication}  data.
 * <p>
 * Note that this view is just a snapshot of active sessions at the time of call, and might not represent a true
 * view of unexpired sessions by the time it is presented to clients.
 * </p>
 * <p>
 * This API returns an un-typed view of this data in a form of a map which adds a flexibility
 * to clients to render it however they choose. As a convenience, this interface also exposes an Enum of the map keys
 * it expects implementors to use.
 * </p>
 * Concurrency semantics: implementations must be thread safe.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface SingleSignOnSessionsReport {

    /**
     * The enum Sso session attribute keys.
     */
    enum SsoSessionAttributeKeys {
        AUTHENTICATED_PRINCIPAL("authenticated_principal"),
        AUTHENTICATION_DATE("authentication_date"),
        NUMBER_OF_USES("number_of_uses");

        private String attributeKey;

        /**
         * Instantiates a new Sso session attribute keys.
         *
         * @param attributeKey the attribute key
         */
        private SsoSessionAttributeKeys(final String attributeKey) {
            this.attributeKey = attributeKey;
        }

        @Override
        public String toString() {
            return this.attributeKey;
        }
    }

    /**
     * Get a collection of active (unexpired) CAS SSO sessions
     * (with their associated authentication and metadata).
     *
     * If there are no active SSO session, return an empty Collection
     * and never return <strong>null</strong>.
     *
     * @return a collection of SSO sessions (represented by Map of its attributes)
     * OR and empty collection if there are no active SSO sessions
     */
    Collection<Map<String, Object>> getActiveSsoSessions();

}
