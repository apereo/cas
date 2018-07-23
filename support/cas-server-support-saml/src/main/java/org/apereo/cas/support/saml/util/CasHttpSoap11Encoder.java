package org.apereo.cas.support.saml.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml1.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.util.SOAPConstants;

/**
 * Override OpenSAML {@link HTTPSOAP11Encoder} such that SOAP-ENV XML namespace prefix is used for SOAP envelope
 * elements. This is needed for backward compatibility with certain CAS clients (e.g. Java CAS client).
 *
 * @author Marvin S. Addison
 * @since 4.2.0
 */
@Slf4j
@NoArgsConstructor
public class CasHttpSoap11Encoder extends HTTPSOAP11Encoder {
    private static final String OPENSAML_11_SOAP_NS_PREFIX = "SOAP-ENV";

    @Override
    protected void buildAndStoreSOAPMessage(final XMLObject payload) {
        val builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

        val envBuilder =
            (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        val envelope = envBuilder.buildObject(
            SOAPConstants.SOAP11_NS, Envelope.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        val bodyBuilder =
            (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
        val body = bodyBuilder.buildObject(
            SOAPConstants.SOAP11_NS, Body.DEFAULT_ELEMENT_LOCAL_NAME, OPENSAML_11_SOAP_NS_PREFIX);

        if (!body.getUnknownXMLObjects().isEmpty()) {
            LOGGER.warn("Existing SOAP Envelope Body already contained children");
        }

        body.getUnknownXMLObjects().add(payload);
        envelope.setBody(body);
        this.storeSOAPEnvelope(envelope);
    }

}
