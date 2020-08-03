package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.serialization.TicketIdSanitizationUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.time.Instant;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultCasEventListener {

    private final CasEventRepository casEventRepository;

    private static CasEvent prepareCasEvent(final AbstractCasEvent event) {
        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        val dt = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp()));
        dto.setCreationTime(dt.toString());

        val clientInfo = ClientInfoHolder.getClientInfo();
        dto.putClientIpAddress(clientInfo.getClientIpAddress());
        dto.putServerIpAddress(clientInfo.getServerIpAddress());
        dto.putAgent(clientInfo.getUserAgent());
        val location = HttpRequestUtils.getHttpServletRequestGeoLocation(clientInfo.getGeoLocation());
        dto.putGeoLocation(location);
        return dto;
    }

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (this.casEventRepository != null) {
            val dto = prepareCasEvent(event);
            dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toString());
            dto.putEventId(TicketIdSanitizationUtils.sanitize(event.getTicketGrantingTicket().getId()));
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
    @Async
    public void handleCasAuthenticationTransactionFailureEvent(final CasAuthenticationTransactionFailureEvent event) {
        if (this.casEventRepository != null) {
            val dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getCredential().getId());
            dto.putEventId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleCasAuthenticationPolicyFailureEvent(final CasAuthenticationPolicyFailureEvent event) {
        if (this.casEventRepository != null) {
            val dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            dto.putEventId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas risky authentication detected event.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void handleCasRiskyAuthenticationDetectedEvent(final CasRiskyAuthenticationDetectedEvent event) {
        if (this.casEventRepository != null) {
            val dto = prepareCasEvent(event);
            dto.putEventId(event.getService().getName());
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            this.casEventRepository.save(dto);
        }
    }
}
