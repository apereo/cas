package org.apereo.cas.configuration.model.core.web.view;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Slf4j
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
}
