package org.apereo.cas.configuration.model.support;

import lombok.Getter;
import lombok.Setter;

/**
 * Webflow configuration properties.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Getter
@Setter
public class WebflowAutoConfigurationProperties {

    /**
     * The order in which the webflow is configured.
     */
    private int order;

    public WebflowAutoConfigurationProperties(final int order) {
        this.order = order;
    }
}
