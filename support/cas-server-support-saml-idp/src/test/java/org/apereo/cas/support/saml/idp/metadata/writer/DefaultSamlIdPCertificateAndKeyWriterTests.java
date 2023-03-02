package org.apereo.cas.support.saml.idp.metadata.writer;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSamlIdPCertificateAndKeyWriterTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAMLMetadata")
public class DefaultSamlIdPCertificateAndKeyWriterTests extends BaseSamlIdPConfigurationTests {
    @Nested
    @Tag("SAMLMetadata")
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata",
        "cas.authn.saml-idp.metadata.core.certificate-algorithm=SHA512withRSA",
        "cas.authn.saml-idp.metadata.core.key-size=4096"
    })
    public class Sha512Settings extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlSelfSignedCertificateWriter")
        private SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter;

        @Test
        public void verifyOperation() throws Exception {
            val privateKey = new StringWriter();
            val certificate = new StringWriter();
            samlSelfSignedCertificateWriter.writeCertificateAndKey(privateKey, certificate);
            assertNotNull(privateKey.toString());
            assertNotNull(certificate.toString());
        }
    }

    @Nested
    @Tag("SAMLMetadata")
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata",
        "cas.authn.saml-idp.metadata.core.certificate-algorithm=SHA256withRSA",
        "cas.authn.saml-idp.metadata.core.key-size=2048"
    })
    public class Sha256Settings extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlSelfSignedCertificateWriter")
        private SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter;

        @Test
        public void verifyOperation() throws Exception {
            val privateKey = new StringWriter();
            val certificate = new StringWriter();
            samlSelfSignedCertificateWriter.writeCertificateAndKey(privateKey, certificate);
            assertNotNull(privateKey.toString());
            assertNotNull(certificate.toString());
        }
    }
}
