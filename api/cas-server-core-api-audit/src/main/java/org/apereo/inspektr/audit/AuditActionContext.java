package org.apereo.inspektr.audit;

import org.apereo.inspektr.common.web.ClientInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable container holding the core elements of an audit-able action that need to be recorded
 * as an audit trail record.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
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

    public AuditActionContext(final AuditActionContext auditActionContext) {
        this(auditActionContext.getPrincipal(), auditActionContext.getResourceOperatedUpon(),
            auditActionContext.getActionPerformed(), auditActionContext.getApplicationCode(),
            auditActionContext.getWhenActionWasPerformed(), auditActionContext.getClientInfo());
    }
}
