package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
@Endpoint(id = "ssoSessions", enableByDefault = false)
public class SingleSignOnSessionsEndpoint extends BaseCasActuatorEndpoint {

    private static final String STATUS = "status";

    private static final String TICKET_GRANTING_TICKET = "ticketGrantingTicket";

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
        val dateFormat = new ISOStandardDateFormat();
        return getNonExpiredTicketGrantingTickets()
            .stream()
            .map(TicketGrantingTicket.class::cast)
            .filter(tgt -> !(option == SsoSessionReportOptions.DIRECT && tgt.getProxiedBy() != null))
            .map(tgt -> {
                val authentication = tgt.getAuthentication();
                val principal = authentication.getPrincipal();
                val sso = new HashMap<String, Object>(SsoSessionAttributeKeys.values().length);
                sso.put(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.getAttributeKey(), principal.getId());
                sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE.getAttributeKey(), authentication.getAuthenticationDate());
                sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE_FORMATTED.getAttributeKey(),
                    dateFormat.format(DateTimeUtils.dateOf(authentication.getAuthenticationDate())));
                sso.put(SsoSessionAttributeKeys.NUMBER_OF_USES.getAttributeKey(), tgt.getCountOfUses());
                sso.put(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.getAttributeKey(), tgt.getId());
                sso.put(SsoSessionAttributeKeys.PRINCIPAL_ATTRIBUTES.getAttributeKey(), principal.getAttributes());
                sso.put(SsoSessionAttributeKeys.AUTHENTICATION_ATTRIBUTES.getAttributeKey(), authentication.getAttributes());
                if (option != SsoSessionReportOptions.DIRECT) {
                    if (tgt.getProxiedBy() != null) {
                        sso.put(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey(), Boolean.TRUE);
                        sso.put(SsoSessionAttributeKeys.PROXIED_BY.getAttributeKey(), tgt.getProxiedBy().getId());
                    } else {
                        sso.put(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey(), Boolean.FALSE);
                    }
                }
                sso.put(SsoSessionAttributeKeys.AUTHENTICATED_SERVICES.getAttributeKey(), tgt.getServices());
                return sso;
            })
            .collect(Collectors.toList());
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
        val sessionsMap = new HashMap<String, Object>();
        val option = SsoSessionReportOptions.valueOf(type);
        val activeSsoSessions = getActiveSsoSessions(option);
        sessionsMap.put("activeSsoSessions", activeSsoSessions);
        val totalTicketGrantingTickets = new AtomicLong();
        val totalProxyGrantingTickets = new AtomicLong();
        val totalUsageCount = new AtomicLong();
        val uniquePrincipals = new HashSet<Object>(activeSsoSessions.size());
        for (val activeSsoSession : activeSsoSessions) {
            if (activeSsoSession.containsKey(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey())) {
                val isProxied = Boolean.valueOf(activeSsoSession.get(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey()).toString());
                if (isProxied) {
                    totalProxyGrantingTickets.incrementAndGet();
                } else {
                    totalTicketGrantingTickets.incrementAndGet();
                    val principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.getAttributeKey()).toString();
                    uniquePrincipals.add(principal);
                }
            } else {
                totalTicketGrantingTickets.incrementAndGet();
                val principal = activeSsoSession.get(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.getAttributeKey()).toString();
                uniquePrincipals.add(principal);
            }
            val uses = Long.parseLong(activeSsoSession.get(SsoSessionAttributeKeys.NUMBER_OF_USES.getAttributeKey()).toString());
            totalUsageCount.getAndAdd(uses);
        }
        sessionsMap.put("totalProxyGrantingTickets", totalProxyGrantingTickets);
        sessionsMap.put("totalTicketGrantingTickets", totalTicketGrantingTickets);
        sessionsMap.put("totalTickets", totalTicketGrantingTickets.longValue() + totalProxyGrantingTickets.longValue());
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
    @DeleteOperation
    public Map<String, Object> destroySsoSession(@Selector final String ticketGrantingTicket) {

        val sessionsMap = new HashMap<String, Object>(1);
        try {
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            sessionsMap.put(STATUS, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
            sessionsMap.put("message", e.getMessage());
        }
        return sessionsMap;
    }

    /**
     * Destroy sso sessions map.
     *
     * @param type     the type
     * @param username the username
     * @return the map
     */
    @DeleteOperation
    public Map<String, Object> destroySsoSessions(@Nullable final String type, @Nullable final String username) {

        if (StringUtils.isBlank(username) && StringUtils.isBlank(type)) {
            return Map.of(STATUS, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(username)) {
            val sessionsMap = new HashMap<String, Object>(1);
            val tickets = centralAuthenticationService.getTickets(ticket -> ticket instanceof TicketGrantingTicket
                && ((TicketGrantingTicket) ticket).getAuthentication().getPrincipal().getId().equalsIgnoreCase(username));
            tickets.forEach(ticket -> sessionsMap.put(ticket.getId(), destroySsoSession(ticket.getId())));
            return sessionsMap;
        }

        val sessionsMap = new HashMap<String, Object>();
        val failedTickets = new HashMap<String, String>();
        val option = SsoSessionReportOptions.valueOf(type);
        val collection = getActiveSsoSessions(option);
        collection
            .stream()
            .map(sso -> sso.get(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.toString()).toString())
            .forEach(ticketGrantingTicket -> {
                try {
                    this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
                } catch (final Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.error(e.getMessage());
                    }
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

    /**
     * The enum SSO session report options.
     */
    @RequiredArgsConstructor
    @Getter
    enum SsoSessionReportOptions {

        ALL("ALL"), PROXIED("PROXIED"), DIRECT("DIRECT");

        private final String type;
    }

    /**
     * The enum Sso session attribute keys.
     */
    @Getter
    enum SsoSessionAttributeKeys {

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
    }
}
