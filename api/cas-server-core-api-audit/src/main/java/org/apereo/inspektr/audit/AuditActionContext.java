package org.apereo.inspektr.audit;

import org.apereo.inspektr.common.web.ClientInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * Immutable container holding the core elements of an audit-able action that need to be recorded
 * as an audit trail record.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
@Getter
@With
@NoArgsConstructor(force = true)
@EqualsAndHashCode
public class AuditActionContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -3530737409883959089L;

    @JsonProperty("principal")
    private final String principal;

    @JsonProperty("auditableResource")
    private final String resourceOperatedUpon;

    @JsonProperty("actionPerformed")
    private final String actionPerformed;

    @JsonProperty("applicationCode")
    private final String applicationCode;

    @JsonProperty("whenActionWasPerformed")
    private final LocalDateTime whenActionWasPerformed;

    @JsonProperty("clientInfo")
    private final ClientInfo clientInfo;

    /**
     * What the action was for, when it was for something that can be counted: the id of the application signed in
     * to, or the multifactor provider that was triggered. Kept apart from the resource, so storages can group by it.
     */
    @JsonProperty("target")
    private final @Nullable String target;

    public AuditActionContext(final String principal, final String resourceOperatedUpon, final String actionPerformed,
                              final String applicationCode, final LocalDateTime whenActionWasPerformed,
                              final ClientInfo clientInfo, @Nullable final String target) {
        this.principal = principal;
        this.resourceOperatedUpon = resourceOperatedUpon;
        this.actionPerformed = actionPerformed;
        this.applicationCode = applicationCode;
        this.whenActionWasPerformed = whenActionWasPerformed;
        this.clientInfo = clientInfo;
        this.target = target;
    }

    public AuditActionContext(final String principal, final String resourceOperatedUpon, final String actionPerformed,
                              final String applicationCode, final LocalDateTime whenActionWasPerformed,
                              final ClientInfo clientInfo) {
        this(principal, resourceOperatedUpon, actionPerformed, applicationCode, whenActionWasPerformed, clientInfo, null);
    }

    public AuditActionContext(final AuditActionContext auditActionContext) {
        this(auditActionContext.getPrincipal(), auditActionContext.getResourceOperatedUpon(),
            auditActionContext.getActionPerformed(), auditActionContext.getApplicationCode(),
            auditActionContext.getWhenActionWasPerformed(), auditActionContext.getClientInfo(),
            auditActionContext.getTarget());
    }
}
