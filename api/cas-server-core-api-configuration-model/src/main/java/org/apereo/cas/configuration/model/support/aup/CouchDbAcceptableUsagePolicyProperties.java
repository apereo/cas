package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-couchdb")
@Accessors(chain = true)
@Getter
@Setter
public class CouchDbAcceptableUsagePolicyProperties extends BaseAsynchronousCouchDbProperties {

    private static final long serialVersionUID = 1323894615409106853L;

    public CouchDbAcceptableUsagePolicyProperties() {
        setDbName("acceptable_usage_policy");
    }
}
