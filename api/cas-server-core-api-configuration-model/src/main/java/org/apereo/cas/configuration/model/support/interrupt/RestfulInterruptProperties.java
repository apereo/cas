package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulInterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RestfulInterruptProperties")
public class RestfulInterruptProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 1833594332973137011L;
}
