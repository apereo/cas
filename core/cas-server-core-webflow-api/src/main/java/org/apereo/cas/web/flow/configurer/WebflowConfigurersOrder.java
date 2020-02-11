package org.apereo.cas.web.flow.configurer;

/**
 * Defines the webflow configurers order.
 *
 * @author Jerome LELEU
 * @since 6.1.0
 */
public interface WebflowConfigurersOrder {

    /**
     * The SPNEGO webflow order.
     */
    int SPNEGO = 100;

    /**
     * The password management webflow order.
     */
    int PM = 200;
}
