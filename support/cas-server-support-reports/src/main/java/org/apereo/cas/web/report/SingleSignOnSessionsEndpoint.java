package org.apereo.cas.web.report;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import javax.servlet.http.HttpServletResponse;
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
@Slf4j
@ToString
@Getter
@Endpoint(id = "sso-sessions", enableByDefault = false)
public class SingleSignOnSessionsEndpoint extends BaseCasMvcEndpoint {

    private static final String STATUS = "status";

    private static final String TICKET_GRANTING_TICKET = "ticketGrantingTicket";

    private enum SsoSessionReportOptions {

        ALL("all"), PROXIED("proxied"), DIRECT("direct");

        private final String type;

        /**
         * Instantiates a new Sso session report options.
         *
         * @param type the type
         */
        SsoSessionReportOptions(final String type) {
            this.type = type;
        }
    }

    /**
     * The enum Sso session attribute keys.
     */
    @Getter
    private enum SsoSessionAttributeKeys {

        AUTHENTICATED_PRINCIPAL("authenticated_principal"), PRINCIPAL_ATTRIBUTES("principal_attributes"),
        AUTHENTICATION_DATE("authentication_date"), AUTHENTICATION_DATE_FORMATTED("authentication_date_formatted"),
        TICKET_GRANTING_TICKET("ticket_granting_ticket"), AUTHENTICATION_ATTRIBUTES("authentication_attributes"),
        PROXIED_BY("proxied_by"), AUTHENTICATED_SERVICES("authenticated_services"),
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
    }

    private final CentralAuthenticationService centralAuthenticationService;

    public SingleSignOnSessionsEndpoint(final CentralAuthenticationService centralAuthenticationService,
                                        final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Gets sso sessions.
     *
     * @param option the option
     * @return the sso sessions
     */
    private Collection<Map<String, Object>> getActiveSsoSessions(final SsoSessionReportOptions option) {
        final Collection<Map<String, Object>> activeSessions = new ArrayList<>();
        final var dateFormat = new ISOStandardDateFormat();
        getNonExpiredTicketGrantingTickets().stream().map(TicketGrantingTicket.class::cast)
            .filter(tgt -> !(option == SsoSessionReportOptions.DIRECT && tgt.getProxiedBy() != null))
            .forEach(tgt -> {
                final var authentication = tgt.getAuthentication();
                final var principal = authentication.getPrincipal();
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
            });
        return activeSessions;
    }

    /**
     * Gets non expired ticket granting tickets.
     *
     * @return the non expired ticket granting tickets
     */
    private Collection<Ticket> getNonExpiredTicketGrantingTickets() {
        return this.centralAuthenticationService.getTickets(ticket -> ticket instanceof TicketGrantingTicket && !ticket.isExpired());
    }

    /**
     * Endpoint for getting SSO Sessions in JSON format.
     *
     * @param type the type
     * @return the sso sessions
     */
    @ReadOperation
    public Map<String, Object> getSsoSessions(final String type) {
        final Map<String, Object> sessionsMap = new HashMap<>(1);
        final var option = SsoSessionReportOptions.valueOf(type);
        final var activeSsoSessions = getActiveSsoSessions(option);
        sessionsMap.put("activeSsoSessions", activeSsoSessions);
        long totalTicketGrantingTickets = 0;
        long totalProxyGrantingTickets = 0;
        long totalUsageCount = 0;
        final Set<String> uniquePrincipals = new HashSet<>();
        for (final var activeSsoSession : activeSsoSessions) {
            if (activeSsoSession.containsKey(SsoSessionAttributeKeys.IS_PROXIED.toString())) {
                final var isProxied = Boolean.valueOf(activeSsoSession.get(SsoSessionAttributeKeys.IS_PROXIED.toString()).toString());
                if (isProxied) {
                    totalProxyGrantingTickets++;
                } else {
                    totalTicketGrantingTickets++;
                    final var principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString()).toString();
                    uniquePrincipals.add(principal);
                }
            } else {
                totalTicketGrantingTickets++;
                final var principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString()).toString();
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
    @WriteOperation
    public Map<String, Object> destroySsoSession(@Selector final String ticketGrantingTicket) {

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
     * Destroy sso sessions map.
     *
     * @param type the type
     * @return the map
     */
    @WriteOperation
    public Map<String, Object> destroySsoSessions(final String type) {

        final Map<String, Object> sessionsMap = new HashMap<>();
        final Map<String, String> failedTickets = new HashMap<>();
        final var option = SsoSessionReportOptions.valueOf(type);
        final var collection = getActiveSsoSessions(option);
        collection
            .stream()
            .map(sso -> sso.get(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.toString()).toString())
            .forEach(ticketGrantingTicket -> {
                try {
                    this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    failedTickets.put(ticketGrantingTicket, e.getMessage());
                }
            });
        if (failedTickets.isEmpty()) {
            sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
        } else {
            sessionsMap.put(STATUS, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put("failedTicketGrantingTickets", failedTickets);
        }
        return sessionsMap;
    }
}
