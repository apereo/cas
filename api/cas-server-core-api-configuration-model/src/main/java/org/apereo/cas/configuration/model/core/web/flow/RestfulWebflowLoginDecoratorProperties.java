package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulWebflowLoginDecoratorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-webflow", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RestfulWebflowLoginDecoratorProperties")
public class RestfulWebflowLoginDecoratorProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -8102345678378393382L;
}
