package org.apereo.cas.support.events.listener;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.serialization.MessageSanitizationUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * This is {@link DefaultCasAuthenticationEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultCasAuthenticationEventListener implements DefaultCasEventListener {

    @NotNull
    private final CasEventRepository casEventRepository;

    private static CasEvent prepareCasEvent(final AbstractCasEvent event) {
        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        val dt = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp()));
        dto.setCreationTime(dt.toString());

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null) {
            dto.putClientIpAddress(clientInfo.getClientIpAddress());
            dto.putServerIpAddress(clientInfo.getServerIpAddress());
            dto.putAgent(clientInfo.getUserAgent());
            val location = HttpRequestUtils.getHttpServletRequestGeoLocation(clientInfo.getGeoLocation());
            dto.putGeoLocation(location);
        } else {
            LOGGER.trace("No client information is available. The final event cannot track client location, user agent or IP addresses");
        }
        return dto;
    }

    @Override
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) throws Exception {
        val dto = prepareCasEvent(event);
        dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toString());
        dto.putEventId(MessageSanitizationUtils.sanitize(event.getTicketGrantingTicket().getId()));
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        this.casEventRepository.save(dto);
    }

    @Override
    public void handleCasTicketGrantingTicketDeletedEvent(final CasTicketGrantingTicketDestroyedEvent event) throws Exception {
        val dto = prepareCasEvent(event);
        dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toString());
        dto.putEventId(MessageSanitizationUtils.sanitize(event.getTicketGrantingTicket().getId()));
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        this.casEventRepository.save(dto);
    }

    @Override
    public void handleCasAuthenticationTransactionFailureEvent(final CasAuthenticationTransactionFailureEvent event) throws Exception {
        val dto = prepareCasEvent(event);
        dto.setPrincipalId(event.getCredential().getId());
        dto.putEventId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
        this.casEventRepository.save(dto);
    }

    @Override
    public void handleCasAuthenticationPolicyFailureEvent(final CasAuthenticationPolicyFailureEvent event) throws Exception {
        val dto = prepareCasEvent(event);
        dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
        dto.putEventId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
        this.casEventRepository.save(dto);
    }

    @Override
    public void handleCasRiskyAuthenticationDetectedEvent(final CasRiskyAuthenticationDetectedEvent event) throws Exception {
        val dto = prepareCasEvent(event);
        dto.putEventId(event.getService().getName());
        dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
        this.casEventRepository.save(dto);
    }
}
