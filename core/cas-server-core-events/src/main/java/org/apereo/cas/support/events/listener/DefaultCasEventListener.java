package org.apereo.cas.support.events.listener;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.TicketIdSanitizationUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultCasEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasEventListener.class);
    
    private final CasEventRepository casEventRepository;
    
    public DefaultCasEventListener(final CasEventRepository casEventRepository) {
        this.casEventRepository = casEventRepository;
    }

    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtInfo(LOGGER, "READY", StringUtils.EMPTY);
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(event.getTimestamp()));
    }
    
    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime());
            dto.putId(TicketIdSanitizationUtils.sanitize(event.getTicketGrantingTicket().getId()));
            dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasAuthenticationTransactionFailureEvent(final CasAuthenticationTransactionFailureEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getCredential().getId());
            dto.putId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasAuthenticationPolicyFailureEvent(final CasAuthenticationPolicyFailureEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            dto.putId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas risky authentication detected event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRiskyAuthenticationDetectedEvent(final CasRiskyAuthenticationDetectedEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.putId(event.getService().getName());
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            this.casEventRepository.save(dto);
        }
    }

    private static CasEvent prepareCasEvent(final AbstractCasEvent event) {
        final CasEvent dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.setCreationTime(DateTimeUtils.zonedDateTimeOf(event.getTimestamp()));

        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        dto.putClientIpAddress(clientInfo.getClientIpAddress());
        dto.putServerIpAddress(clientInfo.getServerIpAddress());
        dto.putAgent(WebUtils.getHttpServletRequestUserAgentFromRequestContext());

        final GeoLocationRequest location = WebUtils.getHttpServletRequestGeoLocationFromRequestContext();
        if (location != null) {
            dto.putGeoLocation(location);
        }
        return dto;
    }

    public CasEventRepository getCasEventRepository() {
        return casEventRepository;
    }
}
