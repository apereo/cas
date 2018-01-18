package org.apereo.cas.configuration.model.core.audit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager;
import java.io.Serializable;

/**
 * This is {@link AuditSlf4jLogProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-audit")
@Slf4j
@Getter
@Setter
public class AuditSlf4jLogProperties implements Serializable {

    private static final long serialVersionUID = 4227475246873515918L;

    /**
     * Indicates whether audit logs should be recorded as a single-line.
     * <p>
     * By default, audit logs are split into multiple lines where each action and activity
     * takes up a full line. This is a more compact version.
     */
    private boolean useSingleLine;

    /**
     * Character to separate audit fields if single-line audits are used.
     */
    private String singlelineSeparator = "|";

    /**
     * The audit format to use in the logs.
     */
    private AbstractStringAuditTrailManager.AuditFormats auditFormat = AbstractStringAuditTrailManager.AuditFormats.DEFAULT;
}
