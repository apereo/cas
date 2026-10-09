package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Settings for DPoP (RFC 9449) proofs that clients present to CAS.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcDPoPProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4120397865123094817L;

    /**
     * Server-provided nonces for DPoP proofs.
     */
    @NestedConfigurationProperty
    private OidcDPoPNonceProperties nonce = new OidcDPoPNonceProperties();
}
