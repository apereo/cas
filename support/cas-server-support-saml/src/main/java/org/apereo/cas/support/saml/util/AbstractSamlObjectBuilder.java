package org.apereo.cas.support.saml.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.HexRandomStringGenerator;
import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.soap.common.SOAPObject;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * An abstract builder to serve as the template handler
 * for SAML1 and SAML2 responses.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
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


    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSamlObjectBuilder.class);

    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    public AbstractSamlObjectBuilder(final OpenSamlConfigBean configBean) {
        this.configBean = configBean;
    }

    /**
     * Create a new SAML object.
     *
     * @param <T>        the generic type
     * @param objectType the object type
     * @return the t
     */
    public <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        try {
            final QName qName = getSamlObjectQName(objectType);
            final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>)
                    XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
            if (builder == null) {
                throw new IllegalStateException("No SAML object builder is registered for class " + objectType.getName());
            }
            return objectType.cast(builder.buildObject(qName));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * New soap object t.
     *
     * @param <T>        the type parameter
     * @param objectType the object type
     * @return the t
     */
    public <T extends SOAPObject> T newSoapObject(final Class<T> objectType) {
        try {
            final QName qName = getSamlObjectQName(objectType);
            final SOAPObjectBuilder<T> builder = (SOAPObjectBuilder<T>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
            if (builder == null) {
                throw new IllegalStateException("No SAML object builder is registered for class " + objectType.getName());
            }
            return objectType.cast(builder.buildObject(qName));
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets saml object QName.
     *
     * @param objectType the object type
     * @return the saml object QName
     */
    public QName getSamlObjectQName(final Class objectType) {
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
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
        final XSAnyBuilder attrValueBuilder = new XSAnyBuilder();
        final XSAny stringValue = attrValueBuilder.buildObject(elementName);
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
            final HexRandomStringGenerator random = new HexRandomStringGenerator(RANDOM_ID_SIZE);
            final String hex = random.getNewString();
            if (StringUtils.isBlank(hex)) {
                throw new IllegalArgumentException("Could not generate a secure random id based on " + random.getAlgorithm());
            }
            return "_" + hex;
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
            final Collection<?> c = (Collection<?>) attributeValue;
            LOGGER.debug("Generating multi-valued SAML attribute [{}] with values [{}]", attributeName, c);
            c.stream().map(value -> newAttributeValue(value, defaultElementName)).forEach(attributeList::add);
        } else {
            LOGGER.debug("Generating SAML attribute [{}] with value [{}]", attributeName, attributeValue);
            attributeList.add(newAttributeValue(attributeValue, defaultElementName));
        }
    }

    /**
     * Sign SAML response.
     *
     * @param samlResponse the SAML response
     * @param privateKey   the private key
     * @param publicKey    the public key
     * @return the response
     */
    public static String signSamlResponse(final String samlResponse, final PrivateKey privateKey, final PublicKey publicKey) {
        final Document doc = constructDocumentFromXml(samlResponse);

        if (doc != null) {
            final org.jdom.Element signedElement = signSamlElement(doc.getRootElement(),
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
            final SAXBuilder builder = new SAXBuilder();
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return builder
                    .build(new ByteArrayInputStream(xmlString.getBytes(Charset.defaultCharset())));
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
            final String providerName = System.getProperty("jsr105Provider", SIGNATURE_FACTORY_PROVIDER_CLASS);

            final XMLSignatureFactory sigFactory = XMLSignatureFactory
                    .getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

            final List<Transform> envelopedTransform = CollectionUtils.wrap(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));

            final Reference ref = sigFactory.newReference(StringUtils.EMPTY, sigFactory
                    .newDigestMethod(DigestMethod.SHA1, null), envelopedTransform, null, null);

            // Create the SignatureMethod based on the type of key
            final SignatureMethod signatureMethod;
            final String algorithm = pubKey.getAlgorithm();
            switch (algorithm) {
                case "DSA":
                    signatureMethod = sigFactory.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
                    break;
                case "RSA":
                    signatureMethod = sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
                    break;
                default:
                    throw new IllegalArgumentException("Error signing SAML element: Unsupported type of key");
            }

            final CanonicalizationMethod canonicalizationMethod = sigFactory
                    .newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                            (C14NMethodParameterSpec) null);

            // Create the SignedInfo
            final SignedInfo signedInfo = sigFactory.newSignedInfo(canonicalizationMethod, signatureMethod, CollectionUtils.wrap(ref));

            // Create a KeyValue containing the DSA or RSA PublicKey
            final KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
            final KeyValue keyValuePair = keyInfoFactory.newKeyValue(pubKey);

            // Create a KeyInfo and add the KeyValue to it
            final KeyInfo keyInfo = keyInfoFactory.newKeyInfo(CollectionUtils.wrap(keyValuePair));
            // Convert the JDOM document to w3c (Java XML signature API requires w3c representation)
            final Element w3cElement = toDom(element);

            // Create a DOMSignContext and specify the DSA/RSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            final DOMSignContext dsc = new DOMSignContext(privKey, w3cElement);

            final Node xmlSigInsertionPoint = getXmlSignatureInsertLocation(w3cElement);
            dsc.setNextSibling(xmlSigInsertionPoint);

            // Marshal, generate (and sign) the enveloped signature
            final XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(dsc);

            return toJdom(w3cElement);

        } catch (final Exception e) {
            throw new IllegalArgumentException("Error signing SAML element: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the xml signature insert location.
     *
     * @param elem the elem
     * @return the xml signature insert location
     */
    private static Node getXmlSignatureInsertLocation(final Element elem) {
        final Node insertLocation;
        NodeList nodeList = elem.getElementsByTagNameNS(SAMLConstants.SAML20P_NS, "Extensions");
        if (nodeList.getLength() != 0) {
            insertLocation = nodeList.item(nodeList.getLength() - 1);
        } else {
            nodeList = elem.getElementsByTagNameNS(SAMLConstants.SAML20P_NS, "Status");
            insertLocation = nodeList.item(nodeList.getLength() - 1);
        }
        return insertLocation;
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
            final XMLOutputter xmlOutputter = new XMLOutputter();
            final StringWriter elemStrWriter = new StringWriter();
            xmlOutputter.output(doc, elemStrWriter);
            final byte[] xmlBytes = elemStrWriter.toString().getBytes(Charset.defaultCharset());
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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

    public OpenSamlConfigBean getConfigBean() {
        return configBean;
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
}

