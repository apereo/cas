package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialDeferredRequest}, the OpenID4VCI 1.0 deferred credential request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OidcVerifiableCredentialDeferredRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5192837465019283746L;

    /**
     * The deferred transaction, as returned by the credential endpoint.
     */
    @JsonProperty("transaction_id")
    private @Nullable String transactionId;

    /**
     * Key and algorithms the wallet wants this response encrypted with, whatever the credential request asked for. A request
     * carrying it must itself be encrypted.
     */
    @JsonProperty("credential_response_encryption")
    private OidcVerifiableCredentialRequest.@Nullable CredentialResponseEncryption credentialResponseEncryption;
}
