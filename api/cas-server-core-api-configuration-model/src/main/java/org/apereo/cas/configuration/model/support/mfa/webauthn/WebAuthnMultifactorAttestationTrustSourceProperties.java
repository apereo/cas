package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link WebAuthnMultifactorAttestationTrustSourceProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnMultifactorAttestationTrustSourceProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -4224840263678287815L;

    /**
     * Trusted device metadata to contain trusted attestation root certificates
     * to pre-seed the metadata service.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties trustedDeviceMetadata = new SpringResourceProperties();

    /**
     * The FIDO Alliance Metadata Service (MDS) is a centralized repository of the Metadata Statement that is
     * used by the relying parties to validate authenticator attestation and prove the genuineness of the device
     * model. MDS also provides information about certification status of the authenticators, and found security
     * issues. Organizations deploying FIDO Authentication are able to use this information to select specific
     * certification levels as required for compliance, and work through the security notifications to ensure effective incident response.
     */
    @NestedConfigurationProperty
    private WebAuthnMultifactorAttestationTrustSourceFidoProperties fido = new WebAuthnMultifactorAttestationTrustSourceFidoProperties();
}
