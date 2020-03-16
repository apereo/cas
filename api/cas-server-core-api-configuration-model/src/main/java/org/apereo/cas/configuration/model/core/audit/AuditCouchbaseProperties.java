package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AuditCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-audit-couchbase")
@Getter
@Setter
@Accessors(chain = true)
public class AuditCouchbaseProperties extends BaseCouchbaseProperties {
    private static final long serialVersionUID = 580545095591694L;

    /**
     * Whether audit records should be executed asynchronously.
     */
    private boolean asynchronous;
}
