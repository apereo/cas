package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.springframework.beans.factory.InitializingBean;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class FileSystemSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    public FileSystemSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context) {
        super(context);
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val encCert = getConfigurationContext().getSamlIdPMetadataLocator().resolveEncryptionCertificate(registeredService);
        val encKey = getConfigurationContext().getSamlIdPMetadataLocator().resolveEncryptionKey(registeredService);
        if (encCert.isFile() && encKey.isFile()) {
            writeCertificateAndKey(encCert.getFile(), encKey.getFile(), registeredService);
        }
        return Pair.of(encCert.getContentAsString(StandardCharsets.UTF_8), encKey.getContentAsString(StandardCharsets.UTF_8));
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val signingCert = getConfigurationContext().getSamlIdPMetadataLocator().resolveSigningCertificate(registeredService);
        val signingKey = getConfigurationContext().getSamlIdPMetadataLocator().resolveSigningKey(registeredService);
        if (signingCert.isFile() && signingKey.isFile()) {
            writeCertificateAndKey(signingCert.getFile(), signingKey.getFile(), registeredService);
        }
        return Pair.of(signingCert.getContentAsString(StandardCharsets.UTF_8), signingKey.getContentAsString(StandardCharsets.UTF_8));
    }

    @Override
    protected String writeMetadata(final String metadata, final Optional<SamlRegisteredService> registeredService) throws Throwable {
        val metadataResource = getConfigurationContext().getSamlIdPMetadataLocator().resolveMetadata(registeredService);
        val metadataFile = metadataResource.getFile();
        LOGGER.info("Writing SAML2 metadata to [{}]", metadataFile);

        val mdProps = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata();
        if (mdProps.getFileSystem().isSignMetadata()) {
            val resolvedCert = getConfigurationContext().getSamlIdPMetadataLocator().resolveSigningCertificate(registeredService);
            val signingCertificate = SamlUtils.readCertificate(resolvedCert);

            val resolvedKey = getConfigurationContext().getSamlIdPMetadataLocator().resolveSigningKey(registeredService);
            val bean = new PrivateKeyFactoryBean();
            bean.setLocation(resolvedKey);
            bean.afterPropertiesSet();
            val signingKey = bean.getObject();

            val signedMetadata = sign(metadata.getBytes(StandardCharsets.UTF_8), signingCertificate, signingKey);
            FileUtils.write(metadataFile, new String(signedMetadata, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } else {
            FileUtils.write(metadataFile, metadata, StandardCharsets.UTF_8);
        }
        LOGGER.info("Wrote SAML2 metadata to [{}]", metadataFile);
        return metadata;
    }

    private byte[] sign(final byte[] metadata, final X509Certificate signingCertificate,
                        final PrivateKey privateKey) throws Exception {
        try (var is = new ByteArrayInputStream(metadata)) {
            val document = getConfigurationContext().getOpenSamlConfigBean().getParserPool().parse(is);
            val documentElement = document.getDocumentElement();
            val unmarshaller = getConfigurationContext().getOpenSamlConfigBean()
                .getUnmarshallerFactory().getUnmarshaller(documentElement);
            val xmlObject = Objects.requireNonNull(unmarshaller).unmarshall(documentElement);
            if (xmlObject instanceof final SignableXMLObject root && !root.isSigned()) {
                val signingParameters = new SignatureSigningParameters();
                val credential = new BasicX509Credential(signingCertificate, privateKey);
                val mgmr = DefaultSecurityConfigurationBootstrap.buildBasicKeyInfoGeneratorManager();
                val keyInfoGenerator = mgmr.getDefaultManager().getFactory(credential).newInstance();
                signingParameters.setKeyInfoGenerator(keyInfoGenerator);
                signingParameters.setSigningCredential(credential);
                signingParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
                signingParameters.setSignatureReferenceDigestMethod(SignatureConstants.ALGO_ID_DIGEST_SHA256);
                signingParameters.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
                SignatureSupport.signObject(root, signingParameters);
                return SamlUtils.transformSamlObject(getConfigurationContext().getOpenSamlConfigBean(), root)
                    .toString().getBytes(StandardCharsets.UTF_8);
            }
            return metadata;
        }
    }

    protected void writeCertificateAndKey(final File certificate, final File key,
                                          final Optional<SamlRegisteredService> registeredService) throws Exception {
        if (certificate.exists()) {
            LOGGER.info("Certificate file [{}] already exists, and will be deleted", certificate.getCanonicalPath());
            FileUtils.forceDelete(certificate);
        }
        if (key.exists()) {
            LOGGER.info("Key file [{}] already exists, and will be deleted", key.getCanonicalPath());
            FileUtils.forceDelete(key);
        }
        LOGGER.debug("Writing SAML2 key file to [{}]", key.getPath());
        LOGGER.debug("Writing SAML2 certificate file to [{}]", certificate.getPath());
        try (val keyWriter = Files.newBufferedWriter(key.toPath(), StandardCharsets.UTF_8);
             val certWriter = Files.newBufferedWriter(certificate.toPath(), StandardCharsets.UTF_8)) {
            getConfigurationContext().getSamlIdPCertificateAndKeyWriter().writeCertificateAndKey(keyWriter, certWriter);
        }
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(u -> generate(Optional.empty()));
    }

    /**
     * Initializes a new Generate saml metadata.
     *
     * @throws Exception the exception
     */
    public void initialize() throws Throwable {
        getConfigurationContext().getSamlIdPMetadataLocator().initialize();
        generate(Optional.empty());
    }
}
