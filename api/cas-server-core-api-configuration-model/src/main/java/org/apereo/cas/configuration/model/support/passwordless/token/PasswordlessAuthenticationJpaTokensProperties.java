package org.apereo.cas.configuration.model.support.passwordless.token;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link PasswordlessAuthenticationJpaTokensProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-passwordless-jpa")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationJpaTokensProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 7647381223153797806L;

    /**
     * Settings that control the background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT15S", "PT2M");
}
