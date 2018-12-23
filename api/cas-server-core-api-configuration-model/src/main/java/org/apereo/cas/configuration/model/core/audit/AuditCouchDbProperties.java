package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AuditCouchDbProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class AuditCouchDbProperties extends BaseAsynchronousCouchDbProperties {
    private static final long serialVersionUID = -5607529769937667881L;

    public AuditCouchDbProperties() {
        setDbName("audit");
    }
}
