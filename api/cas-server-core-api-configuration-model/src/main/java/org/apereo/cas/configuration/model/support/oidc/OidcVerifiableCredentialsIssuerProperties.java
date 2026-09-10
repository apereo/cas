package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialsIssuerProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialsIssuerProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2120371070424785548L;

    /**
     * Maximum number of credential requests accepted in a single batch.
     */
    private int batchSize = 10;

    /**
     * Supported credential configurations keyed by identifier.
     */
    private Map<String, OidcVerifiableCredentialConfigurationProperties> credentialConfigurations = new LinkedHashMap<>();
}
