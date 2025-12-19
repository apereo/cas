package org.apereo.cas.configuration.model.support.pac4j;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationGroovyProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationGroovyProvisioningProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 7179027843747126083L;
}
