package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CouchDbAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 7
 */
@RequiresModule(name = "cas-server-support-aup-couchdb")
@Accessors(chain = true)
@Getter
@Setter
@JsonFilter("CouchDbAcceptableUsagePolicyProperties")
@Deprecated(since = "7.0.0")
public class CouchDbAcceptableUsagePolicyProperties extends BaseAsynchronousCouchDbProperties {

    @Serial
    private static final long serialVersionUID = 1323894615409106853L;

    public CouchDbAcceptableUsagePolicyProperties() {
        setDbName("acceptable_usage_policy");
    }
}
