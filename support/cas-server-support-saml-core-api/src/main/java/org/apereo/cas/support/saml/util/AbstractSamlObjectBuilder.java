package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.HexRandomStringGenerator;
import org.apereo.cas.util.serialization.JacksonXmlSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.xs.XSObject;
import org.jdom2.Document;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.core.xml.schema.impl.XSBase64BinaryBuilder;
import org.opensaml.core.xml.schema.impl.XSBooleanBuilder;
import org.opensaml.core.xml.schema.impl.XSDateTimeBuilder;
import org.opensaml.core.xml.schema.impl.XSIntegerBuilder;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.core.xml.schema.impl.XSURIBuilder;
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
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An abstract builder to serve as the template handler
 * for SAML1 and SAML2 responses.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@RequiredArgsConstructor
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

    private static final String LOG_MESSAGE_ATTR_CREATED = "Created attribute value XMLObject: [{}]";

    /**
     * The Config bean.
     */
    @Getter
    protected final transient OpenSamlConfigBean openSamlConfigBean;

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
            doc.setRootElement(signedElement.detach());
            return new XMLOutputter().outputString(doc);
        }
        throw new IllegalArgumentException("Error signing SAML Response: Null document");
    }

    /**
     * Construct document from xml.
     *
     * @param xmlString the xml string
     * @return the document
     */
    @SuppressWarnings("java:S2755")
    public static Document constructDocumentFromXml(final String xmlString) {
        LOGGER.trace("Attempting to construct an instance of Document from xml: [{}]", xmlString);
        try {
            val builder = new SAXBuilder();
            builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return builder.build(new ByteArrayInputStream(xmlString.getBytes(Charset.defaultCharset())));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
    private static org.jdom2.Element signSamlElement(final org.jdom2.Element element, final PrivateKey privKey, final PublicKey pubKey) {
        try {
            LOGGER.trace("Attempting to sign Element: [{}]", element);
            val providerName = System.getProperty("jsr105Provider", SIGNATURE_FACTORY_PROVIDER_CLASS);

            val clazz = Class.forName(providerName);
            val sigFactory = XMLSignatureFactory
                .getInstance("DOM", (Provider) clazz.getDeclaredConstructor().newInstance());

            val envelopedTransform = CollectionUtils.wrap(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            val ref = sigFactory.newReference(StringUtils.EMPTY, sigFactory
                .newDigestMethod(DigestMethod.SHA1, null), envelopedTransform, null, null);

            val signatureMethod = getSignatureMethodFromPublicKey(pubKey, sigFactory);
            val canonicalizationMethod = sigFactory
                .newCanonicalizationMethod(
                    CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                    (C14NMethodParameterSpec) null);

            val signedInfo = sigFactory.newSignedInfo(canonicalizationMethod, signatureMethod, CollectionUtils.wrap(ref));

            val keyInfoFactory = sigFactory.getKeyInfoFactory();
            val keyValuePair = keyInfoFactory.newKeyValue(pubKey);

            val keyInfo = keyInfoFactory.newKeyInfo(CollectionUtils.wrap(keyValuePair));
            val w3cElement = toDom(element);

            val dsc = new DOMSignContext(privKey, w3cElement);

            val xmlSigInsertionPoint = getXmlSignatureInsertLocation(w3cElement);
            dsc.setNextSibling(xmlSigInsertionPoint);

            val signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(dsc);
            return new DOMBuilder().build(w3cElement);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Error signing SAML element: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    private static SignatureMethod getSignatureMethodFromPublicKey(final PublicKey pubKey,
                                                                   final XMLSignatureFactory sigFactory) {
        val algorithm = pubKey.getAlgorithm();
        if ("DSA".equalsIgnoreCase(algorithm)) {
            return sigFactory.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
        }
        if ("RSA".equalsIgnoreCase(algorithm)) {
            return sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        }
        val format = String.format("Unsupported type of key algorithm: [%s]. Only DSA or RSA are supported", algorithm);
        throw new IllegalArgumentException(format);
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
    private static Element toDom(final org.jdom2.Element element) {
        return Objects.requireNonNull(toDom(element.getDocument())).getDocumentElement();
    }

    /**
     * Convert the received jdom doc to a Document element.
     *
     * @param doc the doc
     * @return the org.w3c.dom. document
     */
    private static org.w3c.dom.Document toDom(final Document doc) {
        try {
            LOGGER.trace("Creating document from: [{}]", doc);
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
            LOGGER.trace("Caught error during creation of org.w3c.dom.Document: e.getMessage()", e);
            return null;
        }
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
        LOGGER.trace("Attempting to create SAMLObject for type: [{}] and QName: [{}]", objectType, qName);
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
        LOGGER.trace("Attempting to create SOAPObject for type: [{}] and QName: [{}]", objectType, qName);
        val builder = (SOAPObjectBuilder<T>)
            XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAML object builder is registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject(qName));
    }

    /**
     * Gets saml object QName indicated by field {@link #DEFAULT_ELEMENT_NAME_FIELD}.
     *
     * @param objectType the object type
     * @return the saml object QName
     */
    public QName getSamlObjectQName(final Class objectType) {
        try {
            val f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
            return (QName) f.get(null);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot find/access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_NAME_FIELD, e);
        }
    }

    /**
     * New attribute value.
     *
     * @param value       the value
     * @param valueType   the value type
     * @param elementName the element name
     * @return the xS string
     */
    protected XMLObject newAttributeValue(final Object value, final String valueType, final QName elementName) {
        LOGGER.trace("Creating new attribute value XMLObject for value: [{}], value type: [{}], QName: [{}]", value, valueType, elementName);
        if (XSString.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSStringBuilder();
            val attrValueObj = builder.buildObject(elementName, XSString.TYPE_NAME);
            attrValueObj.setValue(value.toString());
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSURI.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSURIBuilder();
            val attrValueObj = builder.buildObject(elementName, XSURI.TYPE_NAME);
            attrValueObj.setURI(value.toString());
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSBoolean.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSBooleanBuilder();
            val attrValueObj = builder.buildObject(elementName, XSBoolean.TYPE_NAME);
            attrValueObj.setValue(XSBooleanValue.valueOf(value.toString().toLowerCase()));
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSInteger.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSIntegerBuilder();
            val attrValueObj = builder.buildObject(elementName, XSInteger.TYPE_NAME);
            attrValueObj.setValue(Integer.valueOf(value.toString()));
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSDateTime.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSDateTimeBuilder();
            val attrValueObj = builder.buildObject(elementName, XSDateTime.TYPE_NAME);
            attrValueObj.setValue(ZonedDateTime.parse(value.toString()).toInstant());
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSBase64Binary.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val builder = new XSBase64BinaryBuilder();
            val attrValueObj = builder.buildObject(elementName, XSBase64Binary.TYPE_NAME);
            attrValueObj.setValue(value.toString());
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        if (XSObject.class.getSimpleName().equalsIgnoreCase(valueType)) {
            val mapper = new JacksonXmlSerializer();
            val builder = new XSAnyBuilder();
            val attrValueObj = builder.buildObject(elementName);
            attrValueObj.setTextContent(mapper.writeValueAsString(value));
            LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
            return attrValueObj;
        }

        val builder = new XSAnyBuilder();
        val attrValueObj = builder.buildObject(elementName);
        attrValueObj.setTextContent(value.toString());
        LOGGER.trace(LOG_MESSAGE_ATTR_CREATED, attrValueObj);
        return attrValueObj;
    }

    /**
     * Generate a secure random id.
     *
     * @return the secure id string
     */
    public String generateSecureRandomId() {
        val random = new HexRandomStringGenerator(RANDOM_ID_SIZE);
        val hex = random.getNewString();
        return '_' + hex;
    }

    /**
     * Add attribute values to saml attribute.
     *
     * @param attributeName      the attribute name
     * @param attributeValue     the attribute value
     * @param valueType          the value type
     * @param attributeList      the attribute list
     * @param defaultElementName the default element name
     */
    protected void addAttributeValuesToSamlAttribute(final String attributeName,
                                                     final Object attributeValue,
                                                     final String valueType,
                                                     final List<XMLObject> attributeList,
                                                     final QName defaultElementName) {
        val values = CollectionUtils.toCollection(attributeValue);
        if (values == null || values.isEmpty()) {
            LOGGER.trace("Skipping over SAML attribute [{}] since it has no value", attributeName);
            return;
        }
        LOGGER.trace("Attempting to generate SAML attribute [{}] with value(s) [{}]", attributeName, values);
        val c = (Collection<?>) values;
        LOGGER.debug("Generating multi-valued SAML attribute [{}] with values [{}]", attributeName, c);
        c.stream().map(value -> newAttributeValue(value, valueType, defaultElementName)).forEach(attributeList::add);
    }
}

