package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyWebflowLoginDecoratorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-webflow", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GroovyWebflowLoginDecoratorProperties")
public class GroovyWebflowLoginDecoratorProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 8079027843747126083L;
}
