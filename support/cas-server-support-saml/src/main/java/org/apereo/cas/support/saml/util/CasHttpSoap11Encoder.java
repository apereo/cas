package org.apereo.cas.support.saml.util;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml1.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.util.SOAPConstants;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override OpenSAML {@link HTTPSOAP11Encoder} such that SOAP-ENV XML namespace prefix is used for SOAP envelope
 * elements. This is needed for backward compatibility with certain CAS clients (e.g. Java CAS client).
 *
 * @author Marvin S. Addison
 * @since 4.2.0
 */
public class CasHttpSoap11Encoder extends HTTPSOAP11Encoder {
    private static final String OPENSAML_11_SOAP_NS_PREFIX = "SOAP-ENV";

    private static final Logger LOGGER = LoggerFactory.getLogger(CasHttpSoap11Encoder.class);

    /**
     * Instantiates a new encoder.
     */
    public CasHttpSoap11Encoder() {
        super();
    }

    @Override
    protected void buildAndStoreSOAPMessage(final XMLObject payload) {
        final XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        final SOAPObjectBuilder<Envelope> envBuilder =
                (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        final Envelope envelope = envBuilder.buildObject(
                SOAPConstants.SOAP11_NS, Envelope.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        final SOAPObjectBuilder<Body> bodyBuilder =
                (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
        final Body body = bodyBuilder.buildObject(
                SOAPConstants.SOAP11_NS, Body.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        if (!body.getUnknownXMLObjects().isEmpty()) {
            LOGGER.warn("Existing SOAP Envelope Body already contained children");
        }

        body.getUnknownXMLObjects().add(payload);
        envelope.setBody(body);
        this.storeSOAPEnvelope(envelope);
    }

}
