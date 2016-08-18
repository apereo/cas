package org.apereo.cas.support.saml;

import org.cryptacular.util.CertUtil;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

/**
 * This is {@link SamlUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class SamlUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(SamlUtils.class);
    
    private SamlUtils() {}

    /**
     * Read certificate x 509 certificate.
     *
     * @param resource the resource
     * @return the x 509 certificate
     */
    public static X509Certificate readCertificate(final Resource resource) {
        try(InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new RuntimeException("Error reading certificate " + resource, e);
        }
    }

    /**
     * Transform saml object to String.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @return the string
     * @throws SamlException the saml exception
     */
    public static StringWriter transformSamlObject(final OpenSamlConfigBean configBean, final SAMLObject samlObject) throws SamlException {
        final StringWriter writer = new StringWriter();
        try {
            final Marshaller marshaller = configBean.getMarshallerFactory().getMarshaller(samlObject.getElementQName());
            if (marshaller != null) {
                final Element element = marshaller.marshall(samlObject);
                final DOMSource domSource = new DOMSource(element);

                final StreamResult result = new StreamResult(writer);
                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);
            }
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
        return writer;
    }

    /**
     * Log saml object.
     *
     * @param configBean the config bean
     * @param samlObject the saml object
     * @throws SamlException the saml exception
     */
    public static void logSamlObject(final OpenSamlConfigBean configBean, final SAMLObject samlObject) throws SamlException {
        LOGGER.debug("Logging [{}]\n{}", samlObject.getClass().getName(), transformSamlObject(configBean, samlObject));
    }
}
