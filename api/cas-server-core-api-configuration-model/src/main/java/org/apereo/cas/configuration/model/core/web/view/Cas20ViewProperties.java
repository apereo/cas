package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link Cas20ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Cas20ViewProperties")
public class Cas20ViewProperties implements Serializable {

    private static final long serialVersionUID = -7954879759474698003L;

    /**
     * The relative location of the CAS2 success view bean.
     */
    private String success = "protocol/2.0/casServiceValidationSuccess";

    /**
     * The relative location of the CAS3 failure view bean.
     */
    private String failure = "protocol/2.0/casServiceValidationFailure";

    /**
     * Whether v2 protocol support should be forward compatible
     * to act like v3 and match its response, mainly for attribute release.
     */
    private boolean v3ForwardCompatible = true;

    /**
     * Proxy views and settings.
     */
    @NestedConfigurationProperty
    private Cas20ProxyViewProperties proxy = new Cas20ProxyViewProperties();

}
