package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-oidc-vc")
public class OidcVerifiableCredentialConfigurationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6236647635085072105L;

    private String format;

    private String scope;

    private List<String> cryptographicBindingMethodsSupported = Stream.of("jwk").toList();
    private List<String> credentialSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

    private List<String> proofSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

    private Map<String, OidcVerifiableCredentialClaimProperties> claims = new LinkedHashMap<>();
}
