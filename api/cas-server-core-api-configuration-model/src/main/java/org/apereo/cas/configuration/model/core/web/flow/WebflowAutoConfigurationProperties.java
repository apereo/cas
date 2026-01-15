package org.apereo.cas.configuration.model.core.web.flow;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class WebflowAutoConfigurationProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = 2441628331918226505L;

    /**
     * The order in which the webflow is configured.
     */
    private int order;

    /**
     * Whether webflow auto-configuration should be enabled.
     */
    private boolean enabled = true;
}
