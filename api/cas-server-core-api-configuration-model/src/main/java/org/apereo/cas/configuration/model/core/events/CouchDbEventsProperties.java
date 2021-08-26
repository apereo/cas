package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

@RequiresModule(name = "cas-server-support-events-couchdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CouchDbEventsProperties")
public class CouchDbEventsProperties extends BaseAsynchronousCouchDbProperties {

    private static final long serialVersionUID = -1587160128953366615L;

    public CouchDbEventsProperties() {
        setDbName("events");
    }
}
