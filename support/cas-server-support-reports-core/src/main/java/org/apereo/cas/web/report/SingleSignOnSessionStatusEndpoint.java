package org.apereo.cas.web.report;

import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link SingleSignOnSessionStatusEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RestControllerEndpoint(id = "sso", enableByDefault = false)
@RequiredArgsConstructor
public class SingleSignOnSessionStatusEndpoint {

    private final ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGeneratorProvider;

    private final ObjectProvider<TicketRegistrySupport> ticketRegistrySupportProvider;

    /**
     * Sso status response entity.
     *
     * @param tgc     the tgc
     * @param request the request
     * @return the response entity
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current status of single sign-on", parameters = {
        @Parameter(name = "tgc", required = false),
        @Parameter(name = "request", required = false)
    })
    public ResponseEntity<Map<?, ?>> ssoStatus(
        @RequestParam(name = "tgc", required = false, defaultValue = StringUtils.EMPTY)
        final String tgc,
        final HttpServletRequest request) {

        val ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGeneratorProvider.getObject();
        val tgtId = StringUtils.isNotBlank(tgc)
            ? ticketGrantingTicketCookieGenerator.getCasCookieValueManager().obtainCookieValue(tgc, request)
            : ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isBlank(tgtId)) {
            return ResponseEntity.badRequest().build();
        }
        val ticketRegistrySupport = ticketRegistrySupportProvider.getObject();
        val auth = ticketRegistrySupport.getAuthenticationFrom(tgtId);
        if (auth == null) {
            return ResponseEntity.badRequest().build();
        }
        val ticketState = ticketRegistrySupport.getTicket(tgtId);
        val body = CollectionUtils.wrap("principal", auth.getPrincipal().getId(),
            "authenticationDate", auth.getAuthenticationDate(),
            "ticketGrantingTicketCreationTime", ticketState.getCreationTime(),
            "ticketGrantingTicketPreviousTimeUsed", ticketState.getPreviousTimeUsed(),
            "ticketGrantingTicketLastTimeUsed", ticketState.getLastTimeUsed());
        return ResponseEntity.ok(body);
    }
}
