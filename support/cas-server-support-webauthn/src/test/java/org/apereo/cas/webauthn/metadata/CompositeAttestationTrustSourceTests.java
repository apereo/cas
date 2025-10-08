package org.apereo.cas.webauthn.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAttestationTrustSourceFidoProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.webauthn.attestation.AttestationTrustSource;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeAttestationTrustSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.core.trust-source.fido.legal-header=" + WebAuthnMultifactorAttestationTrustSourceFidoProperties.DEFAULT_LEGAL_HEADER,
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org"
    })
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class CompositeAttestationTrustSourceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .build().toObjectMapper();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnMetadataService")
    private AttestationTrustSource webAuthnMetadataService;

    @Test
    void verifyOperation() throws Throwable {
        val map = MAPPER.readValue(casProperties.getAuthn().getMfa().getWebAuthn().getCore()
            .getTrustSource().getTrustedDeviceMetadata().getLocation().getInputStream(), Map.class);
        val cert = CertUtils.readCertificate(new ByteArrayInputStream(((SequencedCollection) map.get("trustedCertificates")).getFirst()
            .toString().getBytes(StandardCharsets.UTF_8)));

        val result = webAuthnMetadataService.findTrustRoots(List.of(cert), Optional.empty());
        assertFalse(result.getTrustRoots().isEmpty());
    }
}
