package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link AuditCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Since 7.0.0
 */
@RequiresModule(name = "cas-server-support-audit-couchbase")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "7.0.0")
public class AuditCouchbaseProperties extends BaseCouchbaseProperties {
    @Serial
    private static final long serialVersionUID = 580545095591694L;

    /**
     * Whether audit records should be executed asynchronously.
     */
    private boolean asynchronous;
}
