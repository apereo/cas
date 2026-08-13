package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialEncoderFactory}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialEncoderFactory {
    private final CasConfigurationProperties casProperties;

    private final Map<String, OidcVerifiableCredentialEncoder> encoders = new HashMap<>();

    /**
     * Register encoder.
     *
     * @param encoder the encoder
     * @return the oidc verifiable credential encoder factory
     */
    @CanIgnoreReturnValue
    public OidcVerifiableCredentialEncoderFactory register(
        final OidcVerifiableCredentialEncoder encoder) {
        encoders.put(encoder.getFormat().getValue(), encoder);
        return this;
    }

    /**
     * Gets encoder by format.
     *
     * @param format the format
     * @return the encoder
     */
    public OidcVerifiableCredentialEncoder findByFormat(
        final OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats format) {
        return encoders.get(format.getValue());
    }

    /**
     * Find by configuration.
     *
     * @param configurationId the configuration id
     * @return the oidc verifiable credential encoder
     */
    public OidcVerifiableCredentialEncoder findByConfiguration(final String configurationId) {
        val properties = casProperties.getAuthn().getOidc().getVc();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);
        return findByFormat(configuration.getFormat());
    }
}
