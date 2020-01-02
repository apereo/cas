package org.apereo.cas.web.report;

import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link SingleSignOnSessionStatusEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RestControllerEndpoint(id = "sso", enableByDefault = false)
@RequiredArgsConstructor
public class SingleSignOnSessionStatusEndpoint {

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Sso status response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity ssoStatus(final HttpServletRequest request) {
        var tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isBlank(tgtId)) {
            return ResponseEntity.badRequest().build();
        }
        val auth = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
        if (auth == null) {
            return ResponseEntity.badRequest().build();
        }
        val ticketState = this.ticketRegistrySupport.getTicketState(tgtId);
        val body = CollectionUtils.wrap("principal", auth.getPrincipal().getId(),
            "authenticationDate", auth.getAuthenticationDate(),
            "ticketGrantingTicketCreationTime", ticketState.getCreationTime(),
            "ticketGrantingTicketPreviousTimeUsed", ticketState.getPreviousTimeUsed(),
            "ticketGrantingTicketLastTimeUsed", ticketState.getLastTimeUsed());
        return ResponseEntity.ok(body);
    }
}
