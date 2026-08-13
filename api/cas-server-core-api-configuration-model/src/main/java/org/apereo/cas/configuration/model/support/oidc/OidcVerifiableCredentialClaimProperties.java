package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialClaimProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-oidc-vc")
public class OidcVerifiableCredentialClaimProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -611478635714944538L;

    /**
     * Whether this claim is mandatory.
     * A claim (attribute) that must be present in the Verifiable Credential when it is issued.
     * The Issuer requires this data to make the credential valid.
     */
    private boolean mandatory;
    /**
     * A claim that is candidate for Selective Disclosure. It is packaged inside the credential
     * in a way that allows the Holder to selectively share it—or completely
     * hide it—when presenting the credential to a Verifier (Relying Party).
     */
    private boolean disclosable = true;

    /**
     * Control display settings for this claim.
     */
    private List<CredentialClaimDisplay> display = new ArrayList<>();
    
    @Getter
    @Setter
    public static class CredentialClaimDisplay implements Serializable {
        @Serial
        private static final long serialVersionUID = 1912814689105176317L;
        /**
         * Locale that controls this configuration's display.
         */
        private String locale = "en-US";
        /**
         * Display name for this configuration.
         */
        private String name;
    }
}
