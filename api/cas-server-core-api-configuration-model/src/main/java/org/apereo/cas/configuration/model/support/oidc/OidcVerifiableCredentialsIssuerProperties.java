package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
     * Key attestation settings, such as the trust anchors key attestations must chain to.
     */
    @NestedConfigurationProperty
    private OidcVerifiableCredentialKeyAttestationProperties keyAttestation = new OidcVerifiableCredentialKeyAttestationProperties();

    /**
     * Token Status List settings, with which issued credentials can be revoked or suspended.
     */
    @NestedConfigurationProperty
    private OidcVerifiableCredentialStatusListProperties statusList = new OidcVerifiableCredentialStatusListProperties();

    /**
     * Encryption of credential requests and responses, on top of TLS.
     */
    @NestedConfigurationProperty
    private OidcVerifiableCredentialEncryptionProperties encryption = new OidcVerifiableCredentialEncryptionProperties();

    /**
     * Supported credential configurations keyed by identifier.
     */
    private Map<String, OidcVerifiableCredentialConfigurationProperties> credentialConfigurations = new LinkedHashMap<>();

    /**
     * How wallets present this credential issuer, one entry per language. Published as the {@code display}
     * of the credential issuer metadata; wallets show the issuer as unnamed without it.
     */
    private List<IssuerDisplay> display = new ArrayList<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class IssuerDisplay implements Serializable {
        @Serial
        private static final long serialVersionUID = 4475921187209741203L;

        /**
         * Language of this display, as a BCP 47 language tag such as {@code en-US}.
         */
        private String locale;

        /**
         * Display name of the credential issuer.
         */
        private String name;

        /**
         * URL of the credential issuer's logo.
         */
        private String logo;

        /**
         * Alternative text for the logo; defaults to the display name.
         */
        private String logoAltText;
    }
}
