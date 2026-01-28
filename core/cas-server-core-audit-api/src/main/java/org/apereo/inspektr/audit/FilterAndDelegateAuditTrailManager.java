package org.apereo.inspektr.audit;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.RegexUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * This is {@link FilterAndDelegateAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("NullAway.Init")
public class FilterAndDelegateAuditTrailManager implements AuditTrailManager, ApplicationEventPublisherAware {

    private final Collection<AuditTrailManager> auditTrailManagers;

    private final List<String> supportedActionsPerformed;

    private final List<String> excludedActionsPerformed;

    @Setter
    @Nullable
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setAuditFormat(final AuditFormats auditFormat) {
        auditTrailManagers.forEach(mgr -> mgr.setAuditFormat(auditFormat));
    }

    @Override
    public void record(final AuditActionContext ctx) {
        var matched = supportedActionsPerformed
            .stream()
            .anyMatch(action -> {
                var actionPerformed = ctx.getActionPerformed();
                return "*".equals(action) || RegexUtils.createPattern(action).matcher(actionPerformed).find();
            });

        if (matched) {
            matched = excludedActionsPerformed
                .stream()
                .noneMatch(action -> {
                    var actionPerformed = ctx.getActionPerformed();
                    return "*".equals(action) || RegexUtils.createPattern(action).matcher(actionPerformed).find();
                });
        }
        if (matched) {
            LOGGER.trace("Recording audit action context [{}]", ctx);
            auditTrailManagers.forEach(mgr -> mgr.record(ctx));

            if (applicationEventPublisher != null) {
                var auditEvent = new AuditApplicationEvent(ctx.getPrincipal(),
                    ctx.getActionPerformed(),
                    ctx.getApplicationCode(),
                    ctx.getClientInfo().getClientIpAddress(),
                    ctx.getClientInfo().getServerIpAddress(),
                    ctx.getResourceOperatedUpon(),
                    ctx.getWhenActionWasPerformed().toString(),
                    ctx.getClientInfo().getGeoLocation(),
                    ctx.getClientInfo().getUserAgent(),
                    ctx.getClientInfo().getLocale().getLanguage());
                applicationEventPublisher.publishEvent(auditEvent);
            }
        } else {
            LOGGER.trace("Skipping to record audit action context [{}] as it's not authorized as an audit action among [{}]",
                ctx, supportedActionsPerformed);
        }
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> params) {
        return auditTrailManagers
            .stream()
            .map(mgr -> mgr.getAuditRecords(params))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        auditTrailManagers.forEach(AuditTrailManager::removeAll);
    }

}

