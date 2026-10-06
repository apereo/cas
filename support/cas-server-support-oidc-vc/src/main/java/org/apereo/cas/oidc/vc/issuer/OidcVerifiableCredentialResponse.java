package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * This is {@link OidcVerifiableCredentialResponse}, the OpenID4VCI 1.0 credential response, also used by the deferred
 * credential endpoint. The number of entries in {@code credentials} matches the number of proofs the wallet supplied;
 * a deferred response carries {@code transaction_id} and {@code interval} instead.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Jacksonized
public class OidcVerifiableCredentialResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -8698053273429306216L;

    /**
     * The issued credentials.
     */
    @JsonProperty("credentials")
    private List<IssuedCredential> credentials;

    /**
     * Identifies the credentials of this response in notifications the wallet sends to the notification endpoint.
     */
    @JsonProperty("notification_id")
    private String notificationId;

    /**
     * Identifies a deferred transaction, when the credentials are not issued yet; the wallet collects them from the
     * deferred credential endpoint.
     */
    @JsonProperty("transaction_id")
    private String transactionId;

    /**
     * Minimum number of seconds the wallet should wait before asking for the credentials of a deferred transaction.
     */
    @JsonProperty("interval")
    private Long interval;

    /**
     * A single issued credential.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @Jacksonized
    public static class IssuedCredential implements Serializable {
        @Serial
        private static final long serialVersionUID = 4198053273429306217L;

        /**
         * The issued credential, in the format of its credential configuration.
         */
        @JsonProperty("credential")
        private String credential;
    }
}
