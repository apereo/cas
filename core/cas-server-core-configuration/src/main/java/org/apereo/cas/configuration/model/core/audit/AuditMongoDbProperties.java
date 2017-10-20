package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link AuditMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-audit-mongo")
public class AuditMongoDbProperties extends SingleCollectionMongoDbProperties {
    private static final long serialVersionUID = 4940497540189318943L;

    public AuditMongoDbProperties() {
        setCollection("MongoDbCasAuditRepository");
    }
}
