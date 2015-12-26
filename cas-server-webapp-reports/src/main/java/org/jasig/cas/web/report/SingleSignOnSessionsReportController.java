package org.jasig.cas.web.report;

import com.google.common.base.Predicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.ISOStandardDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SSO Report web controller that produces JSON data for the view.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
@Controller("singleSignOnSessionsReportController")
public final class SingleSignOnSessionsReportController {

    private static final String VIEW_SSO_SESSIONS = "monitoring/viewSsoSessions";

    private enum SsoSessionReportOptions {
        ALL("all"),
        PROXIED("proxied"),
        DIRECT("direct");

        private final String type;

        /**
         * Instantiates a new Sso session report options.
         *
         * @param type the type
         */
        SsoSessionReportOptions(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }
    /**
     * The enum Sso session attribute keys.
     */
    private enum SsoSessionAttributeKeys {
        AUTHENTICATED_PRINCIPAL("authenticated_principal"),
        PRINCIPAL_ATTRIBUTES("principal_attributes"),
        AUTHENTICATION_DATE("authentication_date"),
        AUTHENTICATION_DATE_FORMATTED("authentication_date_formatted"),
        TICKET_GRANTING_TICKET("ticket_granting_ticket"),
        AUTHENTICATION_ATTRIBUTES("authentication_attributes"),
        PROXIED_BY("proxied_by"),
        AUTHENTICATED_SERVICES("authenticated_services"),
        IS_PROXIED("is_proxied"),
        NUMBER_OF_USES("number_of_uses");

        private final String attributeKey;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleSignOnSessionsReportController.class);

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    /**
     * Instantiates a new Single sign on sessions report resource.
     */
    public SingleSignOnSessionsReportController() {}

    /**
     * Gets sso sessions.
     *
     * @param option the option
     * @return the sso sessions
     */
    private Collection<Map<String, Object>> getActiveSsoSessions(final SsoSessionReportOptions option) {
        final Collection<Map<String, Object>> activeSessions = new ArrayList<>();
        final ISOStandardDateFormat dateFormat = new ISOStandardDateFormat();

        for (final Ticket ticket : getNonExpiredTicketGrantingTickets()) {
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;

            if (option == SsoSessionReportOptions.DIRECT && tgt.getProxiedBy() != null) {
                continue;
            }

            final Authentication authentication = tgt.getAuthentication();
            final Principal principal = authentication.getPrincipal();

            final Map<String, Object> sso = new HashMap<>(SsoSessionAttributeKeys.values().length);
            sso.put(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString(), principal.getId());
            sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE.toString(), authentication.getAuthenticationDate());
            sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE_FORMATTED.toString(),
                    dateFormat.format(authentication.getAuthenticationDate()));
            sso.put(SsoSessionAttributeKeys.NUMBER_OF_USES.toString(), tgt.getCountOfUses());
            sso.put(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.toString(), tgt.getId());
            sso.put(SsoSessionAttributeKeys.PRINCIPAL_ATTRIBUTES.toString(), principal.getAttributes());
            sso.put(SsoSessionAttributeKeys.AUTHENTICATION_ATTRIBUTES.toString(), authentication.getAttributes());

            if (option != SsoSessionReportOptions.DIRECT) {
                if (tgt.getProxiedBy() != null) {
                    sso.put(SsoSessionAttributeKeys.IS_PROXIED.toString(), Boolean.TRUE);
                    sso.put(SsoSessionAttributeKeys.PROXIED_BY.toString(), tgt.getProxiedBy().getId());
                } else {
                    sso.put(SsoSessionAttributeKeys.IS_PROXIED.toString(), Boolean.FALSE);
                }
            }

            sso.put(SsoSessionAttributeKeys.AUTHENTICATED_SERVICES.toString(), tgt.getServices());

            activeSessions.add(sso);
        }
        return activeSessions;
    }

    /**
     * Gets non expired ticket granting tickets.
     *
     * @return the non expired ticket granting tickets
     */
    private Collection<Ticket> getNonExpiredTicketGrantingTickets() {
        return this.centralAuthenticationService.getTickets(new Predicate<Ticket>() {
            @Override
            public boolean apply(@Nullable final Ticket ticket) {
                if (ticket instanceof TicketGrantingTicket) {
                    return !ticket.isExpired();
                }
                return false;
            }
        });
    }

    /**
     * Endpoint for getting SSO Sessions in JSON format.
     *
     * @param type the type
     * @return the sso sessions
     */
    @RequestMapping(value = "/getSsoSessions", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getSsoSessions(@RequestParam(defaultValue = "ALL") final String type) {
        final Map<String, Object> sessionsMap = new HashMap<>(1);
        final SsoSessionReportOptions option = SsoSessionReportOptions.valueOf(type);

        final Collection<Map<String, Object>> activeSsoSessions = getActiveSsoSessions(option);
        sessionsMap.put("activeSsoSessions", activeSsoSessions);

        long totalTicketGrantingTickets = 0;
        long totalProxyGrantingTickets = 0;
        long totalUsageCount = 0;

        final Set<String> uniquePrincipals = new HashSet<>();

        for (final Map<String, Object> activeSsoSession : activeSsoSessions) {

            if (activeSsoSession.containsKey(SsoSessionAttributeKeys.IS_PROXIED.toString())) {
                final Boolean isProxied = Boolean.valueOf(activeSsoSession.get(SsoSessionAttributeKeys.IS_PROXIED.toString()).toString());
                if (isProxied) {
                    totalProxyGrantingTickets++;
                } else {
                    totalTicketGrantingTickets++;
                    final String principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString()).toString();
                    uniquePrincipals.add(principal);
                }
            } else {
                totalTicketGrantingTickets++;
                final String principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString()).toString();
                uniquePrincipals.add(principal);
            }
            totalUsageCount += Long.parseLong(activeSsoSession.get(SsoSessionAttributeKeys.NUMBER_OF_USES.toString()).toString());

        }

        sessionsMap.put("totalProxyGrantingTickets", totalProxyGrantingTickets);
        sessionsMap.put("totalTicketGrantingTickets", totalTicketGrantingTickets);
        sessionsMap.put("totalTickets", totalTicketGrantingTickets + totalProxyGrantingTickets);
        sessionsMap.put("totalPrincipals", uniquePrincipals.size());
        sessionsMap.put("totalUsageCount", totalUsageCount);
        return sessionsMap;
    }

    /**
     * Endpoint for destroying a single SSO Session.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @return result map
     */
    @RequestMapping(value = "/destroySsoSession", method = RequestMethod.POST)
    @ResponseBody
    public  Map<String, Object> destroySsoSession(@RequestParam final String ticketGrantingTicket) {
        final Map<String, Object> sessionsMap = new HashMap<>(1);
        try {
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            sessionsMap.put("status", HttpServletResponse.SC_OK);
            sessionsMap.put("ticketGrantingTicket", ticketGrantingTicket);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            sessionsMap.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put("ticketGrantingTicket", ticketGrantingTicket);
            sessionsMap.put("message", e.getMessage());
        }
        return sessionsMap;
    }

    /**
     * Endpoint for destroying SSO Sessions.
     *
     * @param type the type
     * @return result map
     */
    @RequestMapping(value = "/destroySsoSessions", method = RequestMethod.POST)
    @ResponseBody
    public  Map<String, Object> destroySsoSessions(@RequestParam(defaultValue = "ALL") final String type) {
        final Map<String, Object> sessionsMap = new HashMap<>();
        final Map<String, String> failedTickets = new HashMap<>();

        final SsoSessionReportOptions option = SsoSessionReportOptions.valueOf(type);
        final Collection<Map<String, Object>> collection = getActiveSsoSessions(option);
        for (final Map<String, Object> sso : collection) {
            final String ticketGrantingTicket =
                    sso.get(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.toString()).toString();
            try {
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                failedTickets.put(ticketGrantingTicket, e.getMessage());
            }
        }

        if (failedTickets.isEmpty()) {
            sessionsMap.put("status", HttpServletResponse.SC_OK);
        } else {
            sessionsMap.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put("failedTicketGrantingTickets", failedTickets);
        }
        return sessionsMap;
    }

    /**
     * Show sso sessions.
     *
     * @return the model and view where json data will be rendered
     * @throws Exception thrown during json processing
     */
    @RequestMapping(value="/statistics/ssosessions", method = RequestMethod.GET)
    public ModelAndView showSsoSessions() throws Exception {
        return new ModelAndView(VIEW_SSO_SESSIONS);
    }
}
