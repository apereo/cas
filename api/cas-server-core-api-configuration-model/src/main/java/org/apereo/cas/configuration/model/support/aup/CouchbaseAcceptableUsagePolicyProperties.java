package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CouchbaseAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 * @deprecated Since 7.0.0
 */
@RequiresModule(name = "cas-server-support-aup-couchbase")
@Accessors(chain = true)
@Getter
@Setter
@JsonFilter("CouchbaseAcceptableUsagePolicyProperties")
@Deprecated(since = "7.0.0")
public class CouchbaseAcceptableUsagePolicyProperties extends BaseCouchbaseProperties {
    @Serial
    private static final long serialVersionUID = 2323894615409106853L;
}
