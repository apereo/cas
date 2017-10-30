package org.apereo.cas.support.saml;

import net.shibboleth.idp.profile.spring.factory.BasicResourceCredentialFactoryBean;
import net.shibboleth.idp.profile.spring.factory.BasicX509CredentialFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.ResourceUtils;
import org.cryptacular.util.CertUtil;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class SamlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlUtils.class);
    private static final int SAML_OBJECT_LOG_ASTERIXLINE_LENGTH = 80;
    private static final String NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    private SamlUtils() {
    }

    /**
     * Read certificate x 509 certificate.
     *
     * @param resource the resource
     * @return the x 509 certificate
     */
    public static X509Certificate readCertificate(final Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Error reading certificate " + resource, e);
        }
    }

    /**
     * Transform saml object into string without indenting the final string.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @return the string writer
     * @throws SamlException the saml exception
     */
    public static StringWriter transformSamlObject(final OpenSamlConfigBean configBean, final XMLObject samlObject) throws SamlException {
        return transformSamlObject(configBean, samlObject, false);
    }

    /**
     * Transform saml object t.
     *
     * @param <T>         the type parameter
     * @param configBean  the config bean
     * @param xml         the xml
     * @return the t
     */
    public static <T extends XMLObject> T transformSamlObject(final OpenSamlConfigBean configBean, final String xml) {
        try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            final Document document = configBean.getParserPool().parse(in);
            final Element root = document.getDocumentElement();

            final Unmarshaller marshaller = configBean.getUnmarshallerFactory().getUnmarshaller(root);
            if (marshaller != null) {
                return (T) marshaller.unmarshall(root);
            }
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Transform saml object to String.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @param indent     the indent
     * @return the string
     * @throws SamlException the saml exception
     */
    public static StringWriter transformSamlObject(final OpenSamlConfigBean configBean, final XMLObject samlObject,
                                                   final boolean indent) throws SamlException {
        final StringWriter writer = new StringWriter();
        try {
            final Marshaller marshaller = configBean.getMarshallerFactory().getMarshaller(samlObject.getElementQName());
            if (marshaller != null) {
                final Element element = marshaller.marshall(samlObject);
                final DOMSource domSource = new DOMSource(element);

                final StreamResult result = new StreamResult(writer);
                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer transformer = tf.newTransformer();

                if (indent) {
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                }
                transformer.transform(domSource, result);
            }
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
        return writer;
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param signatureResourceLocation the signature resource location
     * @return the metadata filter
     * @throws Exception the exception
     */
    public static SignatureValidationFilter buildSignatureValidationFilter(final String signatureResourceLocation) throws Exception {
        final AbstractResource resource = ResourceUtils.getResourceFrom(signatureResourceLocation);
        return buildSignatureValidationFilter(resource);
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param resourceLoader            the resource loader
     * @param signatureResourceLocation the signature resource location
     * @return the metadata filter
     */
    public static SignatureValidationFilter buildSignatureValidationFilter(final ResourceLoader resourceLoader,
                                                                           final String signatureResourceLocation) {
        try {
            final Resource resource = resourceLoader.getResource(signatureResourceLocation);
            return buildSignatureValidationFilter(resource);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param signatureResourceLocation the signature resource location
     * @return the metadata filter
     * @throws Exception the exception
     */
    public static SignatureValidationFilter buildSignatureValidationFilter(final Resource signatureResourceLocation) throws Exception {
        if (!ResourceUtils.doesResourceExist(signatureResourceLocation)) {
            LOGGER.warn("Resource [{}] cannot be located", signatureResourceLocation);
            return null;
        }

        final List<KeyInfoProvider> keyInfoProviderList = new ArrayList<>();
        keyInfoProviderList.add(new RSAKeyValueProvider());
        keyInfoProviderList.add(new DSAKeyValueProvider());
        keyInfoProviderList.add(new DEREncodedKeyValueProvider());
        keyInfoProviderList.add(new InlineX509DataProvider());

        LOGGER.debug("Attempting to resolve credentials from [{}]", signatureResourceLocation);
        final BasicCredential credential = buildCredentialForMetadataSignatureValidation(signatureResourceLocation);
        LOGGER.info("Successfully resolved credentials from [{}]", signatureResourceLocation);

        LOGGER.debug("Configuring credential resolver for key signature trust engine @ [{}]", credential.getCredentialType().getSimpleName());
        final StaticCredentialResolver resolver = new StaticCredentialResolver(credential);
        final BasicProviderKeyInfoCredentialResolver keyInfoResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviderList);
        final ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyInfoResolver);

        LOGGER.debug("Adding signature validation filter based on the configured trust engine");
        final SignatureValidationFilter signatureValidationFilter = new SignatureValidationFilter(trustEngine);
        signatureValidationFilter.setRequireSignedRoot(false);
        LOGGER.debug("Added metadata SignatureValidationFilter with signature from [{}]", signatureResourceLocation);
        return signatureValidationFilter;
    }

    /**
     * Build credential for metadata signature validation basic credential.
     *
     * @param resource the resource
     * @return the basic credential
     * @throws Exception the exception
     */
    public static BasicCredential buildCredentialForMetadataSignatureValidation(final Resource resource) throws Exception {
        try {
            final BasicX509CredentialFactoryBean x509FactoryBean = new BasicX509CredentialFactoryBean();
            x509FactoryBean.setCertificateResource(resource);
            x509FactoryBean.afterPropertiesSet();
            return x509FactoryBean.getObject();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);

            LOGGER.debug("Credential cannot be extracted from [{}] via X.509. Treating it as a public key to locate credential...",
                    resource);
            final BasicResourceCredentialFactoryBean credentialFactoryBean = new BasicResourceCredentialFactoryBean();
            credentialFactoryBean.setPublicKeyInfo(resource);
            credentialFactoryBean.afterPropertiesSet();
            return credentialFactoryBean.getObject();
        }
    }


    /**
     * Log saml object.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @throws SamlException the saml exception
     */
    public static void logSamlObject(final OpenSamlConfigBean configBean, final XMLObject samlObject) throws SamlException {
        LOGGER.debug(StringUtils.repeat('*', SAML_OBJECT_LOG_ASTERIXLINE_LENGTH));
        LOGGER.debug("Logging [{}]\n\n{}\n\n", samlObject.getClass().getName(), transformSamlObject(configBean, samlObject, true));
        LOGGER.debug(StringUtils.repeat('*', SAML_OBJECT_LOG_ASTERIXLINE_LENGTH));
    }
}
