package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SingleSignOnSessionStatusEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Endpoint(id="singleSignOnSessionStatus")
public class SingleSignOnSessionStatusEndpoint extends BaseCasMvcEndpoint {
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;

    public SingleSignOnSessionStatusEndpoint(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                             final TicketRegistrySupport ticketRegistrySupport,
                                             final CasConfigurationProperties casProperties) {
        super(casProperties.getMonitor().getEndpoints().getSingleSignOnStatus(), casProperties);
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    /**
     * Gets yes/no status.
     *
     * @param request  the request
     * @param response the response
     * @return the status
     */
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @ReadOperation(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getStatus(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        response.setStatus(HttpStatus.OK.value());
        final String tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (StringUtils.isBlank(tgtId)) {
            return result(false);
        }
        final Authentication auth = this.ticketRegistrySupport.getAuthenticationFrom(tgtId);
        return result(auth != null);
    }

    private static String result(final boolean res) {
        return BooleanUtils.toStringYesNo(res);
    }
}
