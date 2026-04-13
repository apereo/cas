package org.apereo.cas.oidc.vc.offer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OidcVerifiableCredentialOffer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OidcVerifiableCredentialOffer implements Serializable {
    @Serial
    private static final long serialVersionUID = -700734371623770443L;

    /**
     * The credential issuer identifier.
     */
    @JsonProperty("credential_issuer")
    private String credentialIssuer;

    /**
     * The identifiers of the credential configurations being offered.
     */
    @JsonProperty("credential_configuration_ids")
    private List<String> credentialConfigurationIds;

    /**
     * Optional grants block.
     */
    @JsonProperty("grants")
    private Grants grants = new Grants();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    public static class Grants implements Serializable {
        @Serial
        private static final long serialVersionUID = -4893372167485529534L;

        @JsonProperty("urn:ietf:params:oauth:grant-type:pre-authorized_code")
        private PreAuthorizedCodeGrant preAuthorizedCodeGrant;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Getter
        @Setter
        public static class PreAuthorizedCodeGrant implements Serializable {
            @Serial
            private static final long serialVersionUID = -5053076827220322623L;

            @JsonProperty("pre-authorized_code")
            private String preAuthorizedCode;

            /**
             * Whether a transaction code is required in the token request.
             */
            @JsonProperty("tx_code_required")
            private boolean txCodeRequired = true;

            /**
             * Optional transaction code associated with the issuance transaction.
             * The transaction code is a code that the wallet must present during
             * the pre-authorized code token exchange if tx_code_required is true.
             */
            @JsonProperty("tx_code")
            private String txCode;

            /**
             * Optional issuer state associated with the issuance transaction.
             */
            @JsonProperty("issuer_state")
            private String issuerState;
        }
    }
}
