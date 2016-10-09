package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * SSO Report web controller that produces JSON data for the view.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.1
 */
@Controller("singleSignOnSessionsReportController")
@RequestMapping(value = "/status/ssosessions")
public class SingleSignOnSessionsReportController {

    private static final String VIEW_SSO_SESSIONS = "monitoring/viewSsoSessions";
    private static final String STATUS = "status";
    private static final String TICKET_GRANTING_TICKET = "ticketGrantingTicket";
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleSignOnSessionsReportController.class);

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private enum SsoSessionReportOptions {
        ALL("all"),
        PROXIED("proxied"),
        DIRECT("direct");

        private String type;

        /**
         * Instantiates a new Sso session report options.
         *
         * @param type the type
         */
        SsoSessionReportOptions(final String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
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


    private CentralAuthenticationService centralAuthenticationService;

    private AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * Instantiates a new Single sign on sessions report resource.
     */
    public SingleSignOnSessionsReportController() {
    }

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
                    dateFormat.format(DateTimeUtils.dateOf(authentication.getAuthenticationDate())));
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
        return this.centralAuthenticationService.getTickets(ticket -> {
            if (ticket instanceof TicketGrantingTicket) {
                return !ticket.isExpired();
            }
            return false;
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
    public WebAsyncTask<Map<String, Object>> getSsoSessions(@RequestParam(defaultValue = "ALL") final String type) {

        final Callable<Map<String, Object>> asyncTask = () -> {
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
        };
        return new WebAsyncTask<>(casProperties.getHttpClient().getAsyncTimeout(), asyncTask);
    }

    /**
     * Endpoint for destroying a single SSO Session.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @return result map
     */
    @RequestMapping(value = "/destroySsoSession", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> destroySsoSession(@RequestParam final String ticketGrantingTicket) {
        final Map<String, Object> sessionsMap = new HashMap<>(1);
        try {
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            sessionsMap.put(STATUS, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
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
    public Map<String, Object> destroySsoSessions(@RequestParam(defaultValue = "ALL") final String type) {
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
            sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
        } else {
            sessionsMap.put(STATUS, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showSsoSessions() throws Exception {
        return new ModelAndView(VIEW_SSO_SESSIONS);
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }
}
