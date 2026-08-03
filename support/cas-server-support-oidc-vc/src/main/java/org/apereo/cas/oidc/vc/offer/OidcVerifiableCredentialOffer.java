package org.apereo.cas.oidc.vc.offer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link OidcVerifiableCredentialOffer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @ToString
    public static class Grants implements Serializable {
        @Serial
        private static final long serialVersionUID = -4893372167485529534L;

        @JsonProperty("urn:ietf:params:oauth:grant-type:pre-authorized_code")
        private PreAuthorizedCodeGrant preAuthorizedCodeGrant;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @ToString
        public static class PreAuthorizedCodeGrant implements Serializable {
            @Serial
            private static final long serialVersionUID = -5053076827220322623L;

            @JsonProperty("pre-authorized_code")
            private String preAuthorizedCode;

            /**
             * Optional transaction code associated with the issuance transaction.
             * The transaction code is a code that the wallet must present during
             * the pre-authorized code token exchange.
             */
            @JsonProperty("tx_code")
            private TransactionCode transactionCode;

            /**
             * Optional issuer state associated with the issuance transaction.
             */
            @JsonProperty("issuer_state")
            private String issuerState;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @SuperBuilder
        @ToString
        public static class TransactionCode implements Serializable {
            @Serial
            private static final long serialVersionUID = -5053076827220322623L;

            @JsonProperty("input_mode")
            @Builder.Default
            private String inputMode = "text";

            @JsonIgnore
            private String value;

            private int length;

            @Builder.Default
            private String description = "Enter the transaction code.";
        }
    }
}
