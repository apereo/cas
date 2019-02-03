package org.apereo.cas.web.report;

import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
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
@Slf4j
public class SingleSignOnSessionStatusEndpoint {

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Sso status response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ssoStatus(final HttpServletRequest request) {
        val tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isBlank(tgtId)) {
            return ResponseEntity.ok(BooleanUtils.toStringYesNo(false));
        }
        val auth = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
        return ResponseEntity.ok(BooleanUtils.toStringYesNo(auth != null));
    }
}
