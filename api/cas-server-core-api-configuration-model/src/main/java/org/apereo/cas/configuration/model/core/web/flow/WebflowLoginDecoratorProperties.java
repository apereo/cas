package org.apereo.cas.configuration.model.core.web.flow;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class WebflowLoginDecoratorProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2949978905279568311L;

    /**
     * Path to groovy resource that can decorate the login views and states.
     */
    @NestedConfigurationProperty
    private GroovyWebflowLoginDecoratorProperties groovy = new GroovyWebflowLoginDecoratorProperties();

    /**
     * Path to REST API resource that can decorate the login views and states.
     */
    @NestedConfigurationProperty
    private RestfulWebflowLoginDecoratorProperties rest = new RestfulWebflowLoginDecoratorProperties();

}
