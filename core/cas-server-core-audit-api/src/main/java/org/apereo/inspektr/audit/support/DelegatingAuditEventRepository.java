package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DelegatingAuditEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DelegatingAuditEventRepository implements AuditEventRepository {
    private final ObjectProvider<CasEventRepository> casEventRepository;
    private final AuditEventRepository auditEventRepository = new InMemoryAuditEventRepository();

    @Override
    public void add(final AuditEvent event) {
        casEventRepository.ifAvailable(repo ->
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
                repo.save(casEvent);
            }));
        auditEventRepository.add(event);
    }

    @Override
    public List<AuditEvent> find(final String principal, final Instant after, final String type) {
        val results = new ArrayList<AuditEvent>();
        casEventRepository.ifAvailable(repo -> {
            if (StringUtils.isNotBlank(principal) && after != null && StringUtils.isNotBlank(type)) {
                results.addAll(repo.getEventsOfTypeForPrincipal(type, principal, after.atZone(ZoneOffset.UTC))
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            } else if (StringUtils.isNotBlank(principal) && after != null) {
                results.addAll(repo.getEventsForPrincipal(principal, after.atZone(ZoneOffset.UTC))
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            } else if (StringUtils.isNotBlank(principal) && StringUtils.isNotBlank(type)) {
                results.addAll(repo.getEventsOfTypeForPrincipal(type, principal)
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            } else if (StringUtils.isNotBlank(type) && after != null) {
                results.addAll(repo.getEventsOfType(type, after.atZone(ZoneOffset.UTC))
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            } else if (StringUtils.isNotBlank(principal)) {
                results.addAll(repo.getEventsForPrincipal(principal)
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            } else if (StringUtils.isNotBlank(type)) {
                results.addAll(repo.getEventsOfType(type)
                    .map(DelegatingAuditEventRepository::auditEventMapper).toList());
            }
        });

        if (results.isEmpty()) {
            results.addAll(auditEventRepository.find(principal, after, type));
        }
        return results;
    }

    private static AuditEvent auditEventMapper(final CasEvent event) {
        return new AuditEvent(event.getCreationTime(), event.getPrincipalId(), event.getType(), event.getProperties());
    }
}
