package org.jasig.cas.support.events.listener;

import org.jasig.cas.support.events.CasTicketGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.dao.CasEventDTO;
import org.jasig.cas.support.events.dao.CasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("defaultCasEventListener")
public class DefaultCasEventListener {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    /**
     * Gets the http request based on the
     * {@link org.springframework.web.context.request.RequestContextHolder}.
     *
     * @return the request or null
     */
    protected final HttpServletRequest getRequest() {
        try {
            final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest();
            }
        } catch (final Exception e) {
            logger.trace("Unable to obtain the http request", e);
        }
        return null;
    }

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @TransactionalEventListener
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (casEventRepository != null) {

            final CasEventDTO dto = new CasEventDTO();
            dto.setType(event.getClass().getCanonicalName());
            dto.putTimestamp(event.getTimestamp());
            dto.putCreationTime(event.getTicketGrantingTicket().getCreationTime());
            dto.putId(event.getTicketGrantingTicket().getId());

            final HttpServletRequest request = getRequest();
            if (request != null) {
                String ipAddress = request.getHeader("X-FORWARDED-FOR");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }
                dto.putLocation(ipAddress);
                dto.putAgent(request.getHeader("user-agent"));
            }
            casEventRepository.save(dto);
        }
    }
}
