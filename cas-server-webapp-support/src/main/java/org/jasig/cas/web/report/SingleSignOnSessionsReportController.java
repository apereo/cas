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

package org.jasig.cas.web.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Predicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SSO Report web controller that produces JSON data for the view.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
@Controller("singleSignOnSessionsReportController")
@RequestMapping("/statistics/ssosessions")
public final class SingleSignOnSessionsReportController {

    private static final String VIEW_SSO_SESSIONS = "monitoring/viewSsoSessions";

    /**
     * The enum Sso session attribute keys.
     */
    private enum SsoSessionAttributeKeys {
        AUTHENTICATED_PRINCIPAL("authenticated_principal"),
        AUTHENTICATION_DATE("authentication_date"),
        TICKET_GRANTING_TICKET("ticket_granting_ticket"),
        NUMBER_OF_USES("number_of_uses");

        private String attributeKey;

        /**
         * Instantiates a new Sso session attribute keys.
         *
         * @param attributeKey the attribute key
         */
        SsoSessionAttributeKeys(final String attributeKey) {
            this.attributeKey = attributeKey;
        }

        @Override
        public String toString() {
            return this.attributeKey;
        }
    }

    private static final String ROOT_REPORT_ACTIVE_SESSIONS_KEY = "activeSsoSessions";

    private static final String ROOT_REPORT_NA_KEY = "notAvailable";

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleSignOnSessionsReportController.class);

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Value("${sso.sessions.include.tgt:false}")
    private boolean includeTicketGrantingTicketId;

    /**
     * Instantiates a new Single sign on sessions report resource.
     */
    public SingleSignOnSessionsReportController() {
        this.jsonMapper.disable(SerializationFeature.INDENT_OUTPUT);
        this.jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Gets sso sessions.
     *
     * @return the sso sessions
     */
    private Collection<Map<String, Object>> getSsoSessions() {
        final List<Map<String, Object>> activeSessions = new ArrayList<Map<String, Object>>();

        for(final Ticket ticket : getNonExpiredTicketGrantingTickets()) {
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;

            final Map<String, Object> sso = new HashMap<>(SsoSessionAttributeKeys.values().length);
            sso.put(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString(), tgt.getAuthentication().getPrincipal().getId());
            sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE.toString(), tgt.getAuthentication().getAuthenticationDate());
            sso.put(SsoSessionAttributeKeys.NUMBER_OF_USES.toString(), tgt.getCountOfUses());
            if (this.includeTicketGrantingTicketId) {
                sso.put(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.toString(), tgt.getId());
            }

            activeSessions.add(Collections.unmodifiableMap(sso));
        }
        return Collections.unmodifiableCollection(activeSessions);
    }

    /**
     * Gets non expired ticket granting tickets.
     *
     * @return the non expired ticket granting tickets
     */
    private Collection<Ticket> getNonExpiredTicketGrantingTickets() {
        return this.centralAuthenticationService.getTickets(new Predicate() {
            @Override
            public boolean apply(final Object ticket) {
                if (ticket instanceof TicketGrantingTicket) {
                    return !((TicketGrantingTicket) ticket).isExpired();
                }
                return false;
            }
        });
    }

    /**
     * Show sso sessions.
     *
     * @return the model and view where json data will be rendered
     * @throws Exception thrown during json processing
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showSsoSessions() throws Exception {
        final Map<String, Object> sessionsMap = new HashMap<String, Object>(1);
        sessionsMap.put(ROOT_REPORT_ACTIVE_SESSIONS_KEY, getSsoSessions());
        final String jsonRepresentation = this.jsonMapper.writeValueAsString(sessionsMap);
        final ModelAndView modelAndView = new ModelAndView(VIEW_SSO_SESSIONS);
        modelAndView.addObject(ROOT_REPORT_ACTIVE_SESSIONS_KEY, jsonRepresentation);
        return modelAndView;
    }
}
