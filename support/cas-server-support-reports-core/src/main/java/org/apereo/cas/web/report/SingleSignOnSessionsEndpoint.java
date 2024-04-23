package org.apereo.cas.web.report;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
@RestControllerEndpoint(id = "ssoSessions", enableByDefault = false)
public class SingleSignOnSessionsEndpoint extends BaseCasActuatorEndpoint {
    private static final ISOStandardDateFormat DATE_FORMAT = new ISOStandardDateFormat();

    private static final String STATUS = "status";

    private static final String TICKET_GRANTING_TICKET = "ticketGrantingTicket";

    private static final String MESSAGE_FEATURE_SUPPORTED_TICKET_REGISTRY =
        "The functionality provided here requires that the underlying "
            + "ticket registry and store is able to store, maintain and return a collection tickets that represent the single sign-on session. "
            + "You will not be able to collect and review sessions, if the ticket registry does not have this capability";

    private final ObjectProvider<TicketRegistry> ticketRegistryProvider;

    private final ObjectProvider<SingleLogoutRequestExecutor> singleLogoutRequestExecutor;

    public SingleSignOnSessionsEndpoint(
        final ObjectProvider<TicketRegistry> ticketRegistry,
        final CasConfigurationProperties casProperties,
        final ObjectProvider<SingleLogoutRequestExecutor> singleLogoutRequestExecutor) {
        super(casProperties);
        this.ticketRegistryProvider = ticketRegistry;
        this.singleLogoutRequestExecutor = singleLogoutRequestExecutor;
    }

    /**
     * Gets sso sessions for user.
     *
     * @param username the username
     * @return the sso sessions for user
     */
    @GetMapping(path = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all single sign-on sessions for username. " + MESSAGE_FEATURE_SUPPORTED_TICKET_REGISTRY,
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public Map<String, Object> getSsoSessionsForUser(
        @PathVariable final String username) {
        return getSsoSessions(new SsoSessionsRequest().withUsername(username));
    }

    /**
     * Gets sso sessions for user.
     *
     * @param username the username
     * @return the sso sessions for user
     */
    @DeleteMapping(path = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Destroy all single sign-on sessions for username. " + MESSAGE_FEATURE_SUPPORTED_TICKET_REGISTRY,
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public Map<String, Object> destroySsoSessionsForUser(
        @PathVariable final String username,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        return destroySsoSessions(new SsoSessionsRequest().withUsername(username), request, response);
    }

    /**
     * Gets sso sessions.
     *
     * @param ssoSessionsRequest the request
     * @return the sso sessions
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all single sign-on sessions with the given type. " + MESSAGE_FEATURE_SUPPORTED_TICKET_REGISTRY,
        parameters = {
            @Parameter(name = "type", in = ParameterIn.QUERY, description = "Type of sessions to retrieve (ALL, DIRECT, PROXIED)"),
            @Parameter(name = "username", in = ParameterIn.QUERY, description = "Username assigned to each session"),
            @Parameter(name = "from", in = ParameterIn.QUERY, description = "Starting position/index of the query"),
            @Parameter(name = "count", in = ParameterIn.QUERY, description = "Total number of sessions to return")
        })
    public Map<String, Object> getSsoSessions(
        @Valid
        @ModelAttribute final SsoSessionsRequest ssoSessionsRequest) {
        val sessionsMap = new HashMap<String, Object>();

        val activeSsoSessions = getActiveSsoSessions(ssoSessionsRequest);
        sessionsMap.put("activeSsoSessions", activeSsoSessions);
        val totalTicketGrantingTickets = new AtomicLong();
        val totalProxyGrantingTickets = new AtomicLong();
        val totalUsageCount = new AtomicLong();
        val uniquePrincipals = new HashSet<>(activeSsoSessions.size());
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
     * @param request              the request
     * @param response             the response
     * @return result map
     */
    @DeleteMapping(path = "/{ticketGrantingTicket}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove single sign-on session for ticket id",
        parameters = @Parameter(name = "ticketGrantingTicket", required = true, in = ParameterIn.PATH))
    public Map<String, Object> destroySsoSession(
        @PathVariable final String ticketGrantingTicket,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val sessionsMap = new HashMap<String, Object>(1);
        try {
            val sloRequests = singleLogoutRequestExecutor.getObject().execute(ticketGrantingTicket, request, response);
            sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
            sessionsMap.put("singleLogoutRequests", sloRequests);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            sessionsMap.put(STATUS, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sessionsMap.put(TICKET_GRANTING_TICKET, ticketGrantingTicket);
            sessionsMap.put("message", e.getMessage());
        }
        return sessionsMap;
    }

    /**
     * Destroy sso sessions map.
     *
     * @param ssoSessionsRequest the sso sessions request
     * @param request            the request
     * @param response           the response
     * @return the map
     */
    @Operation(summary = "Remove single sign-on session for type and user",
        parameters = {
            @Parameter(name = "type", in = ParameterIn.QUERY, description = "Type of sessions to retrieve (ALL, DIRECT, PROXIED)"),
            @Parameter(name = "username", in = ParameterIn.QUERY, description = "Username assigned to each session"),
            @Parameter(name = "from", in = ParameterIn.QUERY, description = "Starting position/index of the query"),
            @Parameter(name = "count", in = ParameterIn.QUERY, description = "Total number of sessions to return")
        })
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> destroySsoSessions(
        @Valid final SsoSessionsRequest ssoSessionsRequest,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        if (StringUtils.isBlank(ssoSessionsRequest.getUsername()) && StringUtils.isBlank(ssoSessionsRequest.getType())) {
            return Map.of(STATUS, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(ssoSessionsRequest.getUsername())) {
            val sessionsMap = new HashMap<String, Object>(1);
            var tickets = ticketRegistryProvider.getObject().getSessionsFor(ssoSessionsRequest.getUsername());
            if (ssoSessionsRequest.getFrom() > 0) {
                tickets = tickets.skip(ssoSessionsRequest.getFrom());
            }
            if (ssoSessionsRequest.getCount() > 0) {
                tickets = tickets.limit(ssoSessionsRequest.getCount());
            }
            tickets.forEach(ticket -> sessionsMap.put(ticket.getId(), destroySsoSession(ticket.getId(), request, response)));
            return sessionsMap;
        }

        val sessionsMap = new HashMap<String, Object>();
        val collection = getActiveSsoSessions(ssoSessionsRequest);
        collection
            .stream()
            .map(sso -> sso.get(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET_ID.getAttributeKey()).toString())
            .forEach(ticketGrantingTicket -> destroySsoSession(ticketGrantingTicket, request, response));
        sessionsMap.put(STATUS, HttpServletResponse.SC_OK);
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
        AUTHENTICATION_DATE("authentication_date"),
        CREATION_DATE_FORMATTED("creation_date_formatted"),
        LAST_USED_DATE_FORMATTED("last_used_date_formatted"),
        AUTHENTICATION_DATE_FORMATTED("authentication_date_formatted"),
        TICKET_GRANTING_TICKET_ID("ticket_granting_ticket"),
        AUTHENTICATION_ATTRIBUTES("authentication_attributes"),
        PRINCIPAL_ATTRIBUTES("principal_attributes"),
        PROXIED_BY("proxied_by"),
        AUTHENTICATED_SERVICES("authenticated_services"),
        IS_PROXIED("is_proxied"),
        REMEMBER_ME("remember_me"),
        EXPIRATION_POLICY("expiration_policy"),
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    public static class SsoSessionsRequest implements Serializable {
        @Serial
        private static final long serialVersionUID = 9132400807103771828L;

        private String type = SsoSessionReportOptions.ALL.getType();

        private String username;

        private long from;

        private long count = 1000L;
    }

    private Collection<Map<String, Object>> getActiveSsoSessions(final SsoSessionsRequest ssoSessionsRequest) {
        val option = Optional.ofNullable(ssoSessionsRequest.getType()).map(SsoSessionReportOptions::valueOf).orElse(SsoSessionReportOptions.ALL);
        return getNonExpiredTicketGrantingTickets(ssoSessionsRequest.getFrom(), ssoSessionsRequest.getCount())
            .map(TicketGrantingTicket.class::cast)
            .filter(tgt -> !(option == SsoSessionReportOptions.DIRECT && tgt.getProxiedBy() != null))
            .filter(tgt -> StringUtils.isBlank(ssoSessionsRequest.getUsername())
                || StringUtils.equalsIgnoreCase(ssoSessionsRequest.getUsername(), tgt.getAuthentication().getPrincipal().getId()))
            .map(tgt -> buildSingleSignOnSessionFromTicketGrantingTicket(option, tgt))
            .collect(Collectors.toList());
    }

    private static Map<String, Object> buildSingleSignOnSessionFromTicketGrantingTicket(final SsoSessionReportOptions option,
                                                                                        final TicketGrantingTicket tgt) {
        val authentication = tgt.getAuthentication();
        val principal = authentication.getPrincipal();
        val sso = new HashMap<String, Object>(SsoSessionAttributeKeys.values().length);
        sso.put(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.getAttributeKey(), principal.getId());

        sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE.getAttributeKey(), authentication.getAuthenticationDate());

        sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE_FORMATTED.getAttributeKey(),
            DATE_FORMAT.format(DateTimeUtils.dateOf(authentication.getAuthenticationDate())));

        sso.put(SsoSessionAttributeKeys.CREATION_DATE_FORMATTED.getAttributeKey(),
            DATE_FORMAT.format(DateTimeUtils.dateOf(tgt.getCreationTime())));

        sso.put(SsoSessionAttributeKeys.LAST_USED_DATE_FORMATTED.getAttributeKey(),
            DATE_FORMAT.format(DateTimeUtils.dateOf(tgt.getLastTimeUsed())));

        sso.put(SsoSessionAttributeKeys.NUMBER_OF_USES.getAttributeKey(), tgt.getCountOfUses());
        sso.put(SsoSessionAttributeKeys.TICKET_GRANTING_TICKET_ID.getAttributeKey(), tgt.getId());
        sso.put(SsoSessionAttributeKeys.PRINCIPAL_ATTRIBUTES.getAttributeKey(), principal.getAttributes());
        sso.put(SsoSessionAttributeKeys.AUTHENTICATION_ATTRIBUTES.getAttributeKey(), authentication.getAttributes());

        val policyData = new LinkedHashMap<String, Object>();

        val expirationPolicy = tgt.getExpirationPolicy();
        policyData.put("timeToIdle", expirationPolicy.getTimeToIdle());
        policyData.put("timeToLive", expirationPolicy.getTimeToLive());
        policyData.put("clock", expirationPolicy.getClock().toString());
        policyData.put("name", expirationPolicy.getName());

        Optional.ofNullable(expirationPolicy.toMaximumExpirationTime(tgt)).ifPresent(dt -> policyData.put("maxExpirationTime", dt));
        Optional.ofNullable(expirationPolicy.getIdleExpirationTime(tgt)).ifPresent(dt -> policyData.put("idleExpirationTime", dt));

        sso.put(SsoSessionAttributeKeys.EXPIRATION_POLICY.getAttributeKey(), policyData);
        sso.put(SsoSessionAttributeKeys.REMEMBER_ME.getAttributeKey(),
            CoreAuthenticationUtils.isRememberMeAuthentication(authentication));
        if (option != SsoSessionReportOptions.DIRECT) {
            if (tgt.getProxiedBy() != null) {
                sso.put(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey(), Boolean.TRUE);
                sso.put(SsoSessionAttributeKeys.PROXIED_BY.getAttributeKey(), tgt.getProxiedBy().getId());
            } else {
                sso.put(SsoSessionAttributeKeys.IS_PROXIED.getAttributeKey(), Boolean.FALSE);
            }
        }

        val services = tgt.getServices();
        sso.put(SsoSessionAttributeKeys.AUTHENTICATED_SERVICES.getAttributeKey(), services);
        return sso;
    }

    private Stream<? extends Ticket> getNonExpiredTicketGrantingTickets(final long from, final long count) {
        var tickets = ticketRegistryProvider.getObject().getTickets(ticket -> ticket instanceof TicketGrantingTicket && !ticket.isExpired());
        if (from > 0) {
            tickets = tickets.skip(from);
        }
        if (count > 0) {
            tickets = tickets.limit(count);
        }
        return tickets;
    }

}
