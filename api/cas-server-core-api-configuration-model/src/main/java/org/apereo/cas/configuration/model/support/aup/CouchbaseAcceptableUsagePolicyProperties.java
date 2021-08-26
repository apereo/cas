package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchbaseAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-aup-couchbase")
@Accessors(chain = true)
@Getter
@Setter
@JsonFilter("CouchbaseAcceptableUsagePolicyProperties")
public class CouchbaseAcceptableUsagePolicyProperties extends BaseCouchbaseProperties {
    private static final long serialVersionUID = 2323894615409106853L;
}
