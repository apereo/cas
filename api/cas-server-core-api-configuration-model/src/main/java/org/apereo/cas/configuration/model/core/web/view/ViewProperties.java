package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
public class ViewProperties implements Serializable {

    private static final long serialVersionUID = 2719748442042197738L;

    /**
     * The default redirect URL if none is specified
     * after a successful authentication event.
     */
    private String defaultRedirectUrl;

    /**
     * Comma separated paths to where CAS templates may be found.
     */
    private List<String> templatePrefixes = new ArrayList<>();

    /**
     * CAS2 views and locations.
     */
    @NestedConfigurationProperty
    private Cas20ViewProperties cas2 = new Cas20ViewProperties();

    /**
     * CAS3 views and locations.
     */
    @NestedConfigurationProperty
    private Cas30ViewProperties cas3 = new Cas30ViewProperties();

    /**
     * Resolve CAS views via REST.
     */
    private Rest rest = new Rest();

    @RequiresModule(name = "cas-server-core-web", automated = true)
    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }
}
