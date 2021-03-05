package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link InfluxDbEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-events-influxdb")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("InfluxDbEventsProperties")
public class InfluxDbEventsProperties extends InfluxDbProperties {

    private static final long serialVersionUID = -3918436901491275547L;

    public InfluxDbEventsProperties() {
        setDatabase("CasInfluxDbEvents");
    }
}
