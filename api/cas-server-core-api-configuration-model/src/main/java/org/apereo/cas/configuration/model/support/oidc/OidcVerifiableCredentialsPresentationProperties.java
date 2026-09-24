package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialsPresentationProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialsPresentationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2120371070424785549L;

    /**
     * How the wallet is expected to identify and authenticate CAS as the verifier,
     * expressed as an OpenID4VP client identifier prefix.
     * <p>
     * {@code REDIRECT_URI} requires no key material, but requests made under it cannot be
     * signed, so the authorization request is carried by value in the deep link and no
     * request URI is offered. {@code X509_SAN_DNS} signs the request object with the CAS
     * OpenID Connect signing key and requires that key to carry a certificate chain whose
     * leaf certificate holds a {@code dNSName} subject alternative name matching the host
     * of the verifier; only then can the request object be served by reference.
     */
    private ClientIdentifierPrefixes clientIdentifierPrefix = ClientIdentifierPrefixes.REDIRECT_URI;

    @RequiredArgsConstructor
    @Getter
    @ToString(includeFieldNames = false, of = "value")
    public enum ClientIdentifierPrefixes {
        /**
         * The client identifier is the verifier's response URI. Requests cannot be signed
         * and are therefore always passed to the wallet by value.
         */
        REDIRECT_URI("redirect_uri"),
        /**
         * The client identifier is a DNS name that must match a {@code dNSName} subject
         * alternative name in the leaf certificate used to sign the request object.
         * It requires the CAS OIDC signing key to carry a certificate chain whose leaf
         * certificate holds a {@code dNSName} subject alternative name matching the host of the verifier. CAS refuses to
         * serve a request object when it does not, rather than producing one that wallets silently reject.
         */
        X509_SAN_DNS("x509_san_dns");

        private final String value;
    }
}
