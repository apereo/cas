package org.apereo.cas.configuration.model.support.couchdb;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link BaseAsynchronousCouchDbProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-couchdb-core")
public abstract class BaseAsynchronousCouchDbProperties extends BaseCouchDbProperties {

    private static final long serialVersionUID = -7920471433876478891L;
    /**
     * Make DB updates asynchronously.
     */
    private boolean asynchronous = true;
}
