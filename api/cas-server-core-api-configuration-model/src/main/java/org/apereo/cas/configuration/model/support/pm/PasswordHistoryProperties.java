package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordHistoryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordHistoryProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 2211199066765183587L;

    /**
     * Password history core/common settings.
     */
    @NestedConfigurationProperty
    private PasswordHistoryCoreProperties core = new PasswordHistoryCoreProperties();

    /**
     * Handle password history with Groovy.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovy = new SpringResourceProperties();
}
