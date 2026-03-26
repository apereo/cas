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
     * Supported credential configurations keyed by identifier.
     */
    private Map<String, CredentialConfigurationProperties> credentialConfigurations = new LinkedHashMap<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CredentialConfigurationProperties implements Serializable {
        @Serial
        private static final long serialVersionUID = 6236647635085072105L;

        private String format;
        private String scope;
        private List<String> cryptographicBindingMethodsSupported = new ArrayList<>();
        private List<String> credentialSigningAlgValuesSupported = new ArrayList<>();
        private List<String> proofSigningAlgValuesSupported = new ArrayList<>();
        private Map<String, ClaimProperties> claims = new LinkedHashMap<>();

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class ClaimProperties implements Serializable {
            @Serial
            private static final long serialVersionUID = -611478635714944538L;

            private boolean mandatory;
            private String valueType;
        }
    }
}
