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
 * This is {@link OidcVerifiableCredentialResponse}, the OpenID4VCI 1.0 credential response.
 * The number of entries in {@code credentials} matches the number of proofs the wallet supplied.
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
