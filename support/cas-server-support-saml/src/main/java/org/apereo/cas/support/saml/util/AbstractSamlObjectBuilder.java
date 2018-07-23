package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.HexRandomStringGenerator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.soap.common.SOAPObject;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;

/**
 * An abstract builder to serve as the template handler
 * for SAML1 and SAML2 responses.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@AllArgsConstructor
public abstract class AbstractSamlObjectBuilder implements Serializable {
    /**
     * The constant DEFAULT_ELEMENT_NAME_FIELD.
     */
    protected static final String DEFAULT_ELEMENT_NAME_FIELD = "DEFAULT_ELEMENT_NAME";

    /**
     * The constant DEFAULT_ELEMENT_LOCAL_NAME_FIELD.
     */
    protected static final String DEFAULT_ELEMENT_LOCAL_NAME_FIELD = "DEFAULT_ELEMENT_LOCAL_NAME";

    private static final int RANDOM_ID_SIZE = 16;

    private static final String SIGNATURE_FACTORY_PROVIDER_CLASS = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";

    private static final long serialVersionUID = -6833230731146922780L;


    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    /**
     * Sign SAML response.
     *
     * @param samlResponse the SAML response
     * @param privateKey   the private key
     * @param publicKey    the public key
     * @return the response
     */
    public static String signSamlResponse(final String samlResponse, final PrivateKey privateKey, final PublicKey publicKey) {
        val doc = constructDocumentFromXml(samlResponse);

        if (doc != null) {
            val signedElement = signSamlElement(doc.getRootElement(),
                privateKey, publicKey);
            doc.setRootElement((org.jdom.Element) signedElement.detach());
            return new XMLOutputter().outputString(doc);
        }
        throw new IllegalArgumentException("Error signing SAML Response: Null document");
    }

    /**
     * Construct document from xml string.
     *
     * @param xmlString the xml string
     * @return the document
     */
    public static Document constructDocumentFromXml(final String xmlString) {
        try {
            val builder = new SAXBuilder();
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return builder.build(new ByteArrayInputStream(xmlString.getBytes(Charset.defaultCharset())));
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Sign SAML element.
     *
     * @param element the element
     * @param privKey the priv key
     * @param pubKey  the pub key
     * @return the element
     */
    private static org.jdom.Element signSamlElement(final org.jdom.Element element, final PrivateKey privKey, final PublicKey pubKey) {
        try {
            val providerName = System.getProperty("jsr105Provider", SIGNATURE_FACTORY_PROVIDER_CLASS);

            val clazz = Class.forName(providerName);
            val sigFactory = XMLSignatureFactory
                .getInstance("DOM", (Provider) clazz.getDeclaredConstructor().newInstance());

            val envelopedTransform = CollectionUtils.wrap(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            val ref = sigFactory.newReference(StringUtils.EMPTY, sigFactory
                .newDigestMethod(DigestMethod.SHA1, null), envelopedTransform, null, null);

            // Create the SignatureMethod based on the type of key
            val signatureMethod = getSignatureMethodFromPublicKey(pubKey, sigFactory);
            val canonicalizationMethod = sigFactory
                .newCanonicalizationMethod(
                    CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                    (C14NMethodParameterSpec) null);

            // Create the SignedInfo
            val signedInfo = sigFactory.newSignedInfo(canonicalizationMethod, signatureMethod, CollectionUtils.wrap(ref));

            // Create a KeyValue containing the DSA or RSA PublicKey
            val keyInfoFactory = sigFactory.getKeyInfoFactory();
            val keyValuePair = keyInfoFactory.newKeyValue(pubKey);

            // Create a KeyInfo and add the KeyValue to it
            val keyInfo = keyInfoFactory.newKeyInfo(CollectionUtils.wrap(keyValuePair));
            // Convert the JDOM document to w3c (Java XML signature API requires w3c representation)
            val w3cElement = toDom(element);

            // Create a DOMSignContext and specify the DSA/RSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            val dsc = new DOMSignContext(privKey, w3cElement);

            val xmlSigInsertionPoint = getXmlSignatureInsertLocation(w3cElement);
            dsc.setNextSibling(xmlSigInsertionPoint);

            // Marshal, generate (and sign) the enveloped signature
            val signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(dsc);

            return toJdom(w3cElement);

        } catch (final Exception e) {
            throw new IllegalArgumentException("Error signing SAML element: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    private static SignatureMethod getSignatureMethodFromPublicKey(final PublicKey pubKey,
                                                                   final XMLSignatureFactory sigFactory) {
        val algorithm = pubKey.getAlgorithm();
        if ("DSA".equals(algorithm)) {
            return sigFactory.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
        }
        if ("RSA".equals(algorithm)) {
            return sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        }
        throw new IllegalArgumentException("Error signing SAML element: Unsupported type of key");
    }

    /**
     * Gets the xml signature insert location.
     *
     * @param elem the elem
     * @return the xml signature insert location
     */
    private static Node getXmlSignatureInsertLocation(final Element elem) {
        val nodeListExtensions = elem.getElementsByTagNameNS(SAMLConstants.SAML20P_NS, "Extensions");
        if (nodeListExtensions.getLength() != 0) {
            return nodeListExtensions.item(nodeListExtensions.getLength() - 1);
        }
        val nodeListStatus = elem.getElementsByTagNameNS(SAMLConstants.SAML20P_NS, "Status");
        return nodeListStatus.item(nodeListStatus.getLength() - 1);
    }

    /**
     * Convert the received jdom element to an Element.
     *
     * @param element the element
     * @return the org.w3c.dom. element
     */
    private static Element toDom(final org.jdom.Element element) {
        return toDom(element.getDocument()).getDocumentElement();
    }

    /**
     * Convert the received jdom doc to a Document element.
     *
     * @param doc the doc
     * @return the org.w3c.dom. document
     */
    private static org.w3c.dom.Document toDom(final Document doc) {
        try {
            val xmlOutputter = new XMLOutputter();
            val elemStrWriter = new StringWriter();
            xmlOutputter.output(doc, elemStrWriter);
            val xmlBytes = elemStrWriter.toString().getBytes(Charset.defaultCharset());
            val dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://apache.org/xml/features/validation/schema/normalized-value", false);
            dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert to a jdom element.
     *
     * @param e the e
     * @return the element
     */
    private static org.jdom.Element toJdom(final Element e) {
        return new DOMBuilder().build(e);
    }

    /**
     * Create a new SAML object.
     *
     * @param <T>        the generic type
     * @param objectType the object type
     * @return the t
     */
    @SneakyThrows
    public <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        val qName = getSamlObjectQName(objectType);
        val builder = (SAMLObjectBuilder<T>)
            XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAML object builder is registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject(qName));
    }

    /**
     * New soap object t.
     *
     * @param <T>        the type parameter
     * @param objectType the object type
     * @return the t
     */
    @SneakyThrows
    public <T extends SOAPObject> T newSoapObject(final Class<T> objectType) {
        val qName = getSamlObjectQName(objectType);
        val builder = (SOAPObjectBuilder<T>)
            XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAML object builder is registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject(qName));
    }

    /**
     * Gets saml object QName.
     *
     * @param objectType the object type
     * @return the saml object QName
     */
    public QName getSamlObjectQName(final Class objectType) {
        try {
            val f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
            return (QName) f.get(null);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find field " + objectType.getName() + '.' + DEFAULT_ELEMENT_NAME_FIELD, e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_NAME_FIELD, e);
        }
    }

    /**
     * New attribute value.
     *
     * @param value       the value
     * @param elementName the element name
     * @return the xS string
     */
    protected XMLObject newAttributeValue(final Object value, final QName elementName) {
        //final XSStringBuilder attrValueBuilder = new XSStringBuilder();
        val attrValueBuilder = new XSAnyBuilder();
        val stringValue = attrValueBuilder.buildObject(elementName);
        stringValue.setTextContent(value.toString());
        return stringValue;
    }

    /**
     * Generate a secure random id.
     *
     * @return the secure id string
     */
    public String generateSecureRandomId() {
        try {
            val random = new HexRandomStringGenerator(RANDOM_ID_SIZE);
            val hex = random.getNewString();
            if (StringUtils.isBlank(hex)) {
                throw new IllegalArgumentException("Could not generate a secure random id based on " + random.getAlgorithm());
            }
            return '_' + hex;
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot create secure random ID generator for SAML message IDs.", e);
        }
    }

    /**
     * Add attribute values to saml attribute.
     *
     * @param attributeName      the attribute name
     * @param attributeValue     the attribute value
     * @param attributeList      the attribute list
     * @param defaultElementName the default element name
     */
    protected void addAttributeValuesToSamlAttribute(final String attributeName,
                                                     final Object attributeValue,
                                                     final List<XMLObject> attributeList,
                                                     final QName defaultElementName) {
        if (attributeValue == null) {
            LOGGER.debug("Skipping over SAML attribute [{}] since it has no value", attributeName);
            return;
        }

        LOGGER.debug("Attempting to generate SAML attribute [{}] with value(s) [{}]", attributeName, attributeValue);
        if (attributeValue instanceof Collection<?>) {
            val c = (Collection<?>) attributeValue;
            LOGGER.debug("Generating multi-valued SAML attribute [{}] with values [{}]", attributeName, c);
            c.stream().map(value -> newAttributeValue(value, defaultElementName)).forEach(attributeList::add);
        } else {
            LOGGER.debug("Generating SAML attribute [{}] with value [{}]", attributeName, attributeValue);
            attributeList.add(newAttributeValue(attributeValue, defaultElementName));
        }
    }

    public OpenSamlConfigBean getConfigBean() {
        return configBean;
    }
}

