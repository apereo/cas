package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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

    private static final long serialVersionUID = 2949978905279568311L;

    /**
     * Path to groovy resource that can decorate the login views and states.
     */
    private Groovy groovy = new Groovy();

    /**
     * Path to REST API resource that can decorate the login views and states.
     */
    private Rest rest = new Rest();

    @RequiresModule(name = "cas-server-core-webflow", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-core-webflow", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }
}
