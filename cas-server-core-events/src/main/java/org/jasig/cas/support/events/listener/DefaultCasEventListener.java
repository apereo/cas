package org.jasig.cas.support.events.listener;

import org.jasig.cas.support.events.CasTicketGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.dao.CasEvent;
import org.jasig.cas.support.events.dao.CasEventRepository;
import org.jasig.cas.util.http.HttpRequestGeoLocation;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.inspektr.common.web.ClientInfo;
import org.jasig.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultCasEventListener")
public class DefaultCasEventListener {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @TransactionalEventListener
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (casEventRepository != null) {

            final CasEvent dto = new CasEvent();
            dto.setType(event.getClass().getCanonicalName());
            dto.putTimestamp(event.getTimestamp());
            dto.putCreationTime(event.getTicketGrantingTicket().getCreationTime());
            dto.putId(event.getTicketGrantingTicket().getId());
            dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());

            final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
            dto.putClientIpAddress(clientInfo.getClientIpAddress());
            dto.putServerIpAddress(clientInfo.getServerIpAddress());
            dto.putAgent(WebUtils.getHttpServletRequestUserAgent());

            final HttpRequestGeoLocation location = WebUtils.getHttpServletRequestGeoLocation();
            dto.putGeoLocation(location);

            casEventRepository.save(dto);
        }
    }
}
