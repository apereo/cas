package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcIdentityAssuranceProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)

public class OidcIdentityAssuranceProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 223128625694269276L;

    /**
     * Assurance verification properties.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties verificationSource = new SpringResourceProperties();
}
