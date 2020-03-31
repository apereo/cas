package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AuditCouchDbProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-audit-couchdb")
@Getter
@Setter
@Accessors(chain = true)
public class AuditCouchDbProperties extends BaseAsynchronousCouchDbProperties {
    private static final long serialVersionUID = -5607529769937667881L;

    public AuditCouchDbProperties() {
        setDbName("audit");
    }
}
