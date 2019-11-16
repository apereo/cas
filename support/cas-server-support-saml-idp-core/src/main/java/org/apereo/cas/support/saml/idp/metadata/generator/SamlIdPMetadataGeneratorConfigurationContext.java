package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.io.ResourceLoader;

/**
 * This is {@link SamlIdPMetadataGeneratorConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class SamlIdPMetadataGeneratorConfigurationContext {
    private final SamlIdPMetadataLocator samlIdPMetadataLocator;
    private final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter;
    private final CipherExecutor<String, String> metadataCipherExecutor;
    private final ResourceLoader resourceLoader;
    private final CasConfigurationProperties casProperties;
}
