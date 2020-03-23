package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link Pac4jDelegatedAuthenticationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-pac4j")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationProvisioningProperties implements Serializable {
    private static final long serialVersionUID = 3478567744591488495L;

    /**
     * Hand off the provisioning task to an external rest api
     * to create and manage establish profiles.
     */
    private Rest rest = new Rest();

    /**
     * Hand off the provisioning task to an external groovy script
     * to create and manage establish profiles.
     */
    private Groovy groovy = new Groovy();

    @RequiresModule(name = "cas-server-support-pac4j")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 7179027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-pac4j")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }
}
