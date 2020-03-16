package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Webflow configuration properties.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-webflow", automated = true)
@Accessors(chain = true)
public class WebflowAutoConfigurationProperties implements Serializable {

    private static final long serialVersionUID = 2441628331918226505L;

    /**
     * The order in which the webflow is configured.
     */
    private int order;

    public WebflowAutoConfigurationProperties(final int order) {
        this.order = order;
    }
}
