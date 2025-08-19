package org.apereo.inspektr.audit.support;

import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This is {@link DelegatingAuditEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DelegatingAuditEventRepository implements AuditEventRepository {
    public static final Function<CasEvent, AuditEvent> AUDIT_EVENT_MAPPER = event ->
        new AuditEvent(event.getCreationTime(), event.getPrincipalId(), event.getType(), event.getProperties());
    private final CasEventRepository casEventRepository;
    private final AuditEventRepository auditEventRepository = new InMemoryAuditEventRepository();

    @Override
    public void add(final AuditEvent event) {
        FunctionUtils.doUnchecked(__ -> {
            val clientInfo = ClientInfoHolder.getClientInfo();
            val casEvent = new CasEvent();
            casEvent.setPrincipalId(event.getPrincipal());
            casEvent.setType(event.getType());
            casEvent.putTimestamp(event.getTimestamp().toEpochMilli());
            casEvent.setCreationTime(event.getTimestamp());

            if (clientInfo != null) {
                casEvent.putClientIpAddress(clientInfo.getClientIpAddress());
                casEvent.putServerIpAddress(clientInfo.getServerIpAddress());
                casEvent.putAgent(clientInfo.getUserAgent());
                val geoLocationRequest = HttpRequestUtils.getHttpServletRequestGeoLocation(clientInfo.getGeoLocation());
                casEvent.putGeoLocation(geoLocationRequest);
                casEvent.putTenant(clientInfo.getTenant());
            }
            casEventRepository.save(casEvent);
            auditEventRepository.add(event);
        });
    }

    @Override
    public List<AuditEvent> find(final String principal, final Instant after, final String type) {
        val results = new ArrayList<AuditEvent>();
        if (StringUtils.isNotBlank(principal) && after != null && StringUtils.isNotBlank(type)) {
            results.addAll(casEventRepository.getEventsOfTypeForPrincipal(type, principal, after.atZone(ZoneOffset.UTC)).map(AUDIT_EVENT_MAPPER).toList());
        } else if (StringUtils.isNotBlank(principal) && after != null) {
            results.addAll(casEventRepository.getEventsForPrincipal(principal, after.atZone(ZoneOffset.UTC)).map(AUDIT_EVENT_MAPPER).toList());
        } else if (StringUtils.isNotBlank(principal) && StringUtils.isNotBlank(type)) {
            results.addAll(casEventRepository.getEventsOfTypeForPrincipal(type, principal).map(AUDIT_EVENT_MAPPER).toList());
        } else if (StringUtils.isNotBlank(type) && after != null) {
            results.addAll(casEventRepository.getEventsOfType(type, after.atZone(ZoneOffset.UTC)).map(AUDIT_EVENT_MAPPER).toList());
        } else if (StringUtils.isNotBlank(principal)) {
            results.addAll(casEventRepository.getEventsForPrincipal(principal).map(AUDIT_EVENT_MAPPER).toList());
        } else if (StringUtils.isNotBlank(type)) {
            results.addAll(casEventRepository.getEventsOfType(type).map(AUDIT_EVENT_MAPPER).toList());
        }

        if (results.isEmpty()) {
            results.addAll(auditEventRepository.find(principal, after, type));
        }
        return results;
    }
}
