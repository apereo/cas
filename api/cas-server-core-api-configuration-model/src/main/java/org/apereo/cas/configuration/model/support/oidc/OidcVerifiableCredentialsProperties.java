package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link OidcVerifiableCredentialsProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialsProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2120371070424785548L;

    /**
     * OIDC verifiable credentials issuer settings.
     */
    @NestedConfigurationProperty
    private OidcVerifiableCredentialsIssuerProperties issuer = new OidcVerifiableCredentialsIssuerProperties();
}
