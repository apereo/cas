package org.apereo.cas.configuration.model.support.couchdb;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseAsyncronousCouchDbProperties}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public abstract class BaseAsyncronousCouchDbProperties extends BaseCouchDbProperties {

    /**
     * Make DB updates asyncronously.
     */
    private boolean asyncronous = true;
}
