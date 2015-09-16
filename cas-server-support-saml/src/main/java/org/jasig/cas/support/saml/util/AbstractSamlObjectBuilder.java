/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.support.saml.util;

import org.bouncycastle.util.encoders.Hex;
import org.jdom.Document;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;
/**
 * An abstract builder to serve as the template handler
 * for SAML1 and SAML2 responses.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public abstract class AbstractSamlObjectBuilder {
    /**
     * The constant DEFAULT_ELEMENT_NAME_FIELD.
     */
    protected static final String DEFAULT_ELEMENT_NAME_FIELD = "DEFAULT_ELEMENT_NAME";

    /**
     * The constant DEFAULT_ELEMENT_LOCAL_NAME_FIELD.
     */
    protected static final String DEFAULT_ELEMENT_LOCAL_NAME_FIELD = "DEFAULT_ELEMENT_LOCAL_NAME";

    private static final int RANDOM_ID_SIZE = 16;

    private  static final String SIGNATURE_FACTORY_PROVIDER_CLASS = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a new SAML object.
     *
     * @param <T> the generic type
     * @param objectType the object type
     * @return the t
     */
    public final <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        final QName qName = getSamlObjectQName(objectType);
        final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAMLObjectBuilder registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject(qName));
    }

    /**
     * Gets saml object QName.
     *
     * @param objectType the object type
     * @return the saml object QName
     * @throws RuntimeException the exception
     */
    public QName getSamlObjectQName(final Class objectType) throws RuntimeException {
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
            return (QName) f.get(null);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find field " + objectType.getName() + '.' + DEFAULT_ELEMENT_NAME_FIELD);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_NAME_FIELD);
        }
    }

    /**
     * Build the saml object based on its QName.
     *
     * @param objectType the object
     * @param qName the QName
     * @param <T> the object type
     * @return the saml object
     */
    private <T extends SAMLObject> T newSamlObject(final Class<T> objectType, final QName qName) {
        final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAMLObjectBuilder registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject());
    }

    /**
     * New attribute value.
     *
     * @param value the value
     * @param elementName the element name
     * @return the xS string
     */
    protected final XSString newAttributeValue(final Object value, final QName elementName) {
        final XSStringBuilder attrValueBuilder = new XSStringBuilder();
        final XSString stringValue = attrValueBuilder.buildObject(elementName, XSString.TYPE_NAME);
        if (value instanceof String) {
            stringValue.setValue((String) value);
        } else {
            stringValue.setValue(value.toString());
        }
        return stringValue;
    }

    /**
     * Generate a secure random id.
     *
     * @return the secure id string
     */
    public String generateSecureRandomId() {
        try {
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            final byte[] buf = new byte[RANDOM_ID_SIZE];
            random.nextBytes(buf);
            return "_".concat(new String(Hex.encode(buf)));
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot create secure random ID generator for SAML message IDs.", e);
        }
    }

    /**
     * Marshal the saml xml object to raw xml.
     *
     * @param object the object
     * @param writer the writer
     * @return the xml string
     */
    public String marshalSamlXmlObject(final XMLObject object, final StringWriter writer)  {
        try {
            final MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            final Marshaller marshaller = marshallerFactory.getMarshaller(object);
            if (marshaller == null) {
                throw new IllegalArgumentException("Cannot obtain marshaller for object " + object.getElementQName());
            }
            final Element element = marshaller.marshall(object);
            element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", SAMLConstants.SAML20_NS);
            element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xenc", "http://www.w3.org/2001/04/xmlenc#");

            final TransformerFactory transFactory = TransformerFactory.newInstance();
            final Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (final Exception e) {
            throw new IllegalStateException("An error has occurred while marshalling SAML object to xml", e);
        }
    }

    /**
     * Sign SAML response.
     *
     * @param samlResponse the SAML response
     * @param privateKey the private key
     * @param publicKey the public key
     * @return the response
     */
    public final String signSamlResponse(final String samlResponse,
                                         final PrivateKey privateKey, final PublicKey publicKey) {
        final Document doc = constructDocumentFromXml(samlResponse);

        if (doc != null) {
            final org.jdom.Element signedElement = signSamlElement(doc.getRootElement(),
                    privateKey, publicKey);
            doc.setRootElement((org.jdom.Element) signedElement.detach());
            return new XMLOutputter().outputString(doc);
        }
        throw new RuntimeException("Error signing SAML Response: Null document");
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
     * @param pubKey the pub key
     * @return the element
     */
    private org.jdom.Element signSamlElement(final org.jdom.Element element, final PrivateKey privKey,
                                                    final PublicKey pubKey) {
        try {
            final String providerName = System.getProperty("jsr105Provider",
                    SIGNATURE_FACTORY_PROVIDER_CLASS);

            final XMLSignatureFactory sigFactory = XMLSignatureFactory
                    .getInstance("DOM", (Provider) Class.forName(providerName)
                            .newInstance());

            final List<Transform> envelopedTransform = Collections
                    .singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                            (TransformParameterSpec) null));

            final Reference ref = sigFactory.newReference("", sigFactory
                            .newDigestMethod(DigestMethod.SHA1, null), envelopedTransform,
                    null, null);

            // Create the SignatureMethod based on the type of key
            final SignatureMethod signatureMethod;
            if (pubKey instanceof DSAPublicKey) {
                signatureMethod = sigFactory.newSignatureMethod(
                        SignatureMethod.DSA_SHA1, null);
            } else if (pubKey instanceof RSAPublicKey) {
                signatureMethod = sigFactory.newSignatureMethod(
                        SignatureMethod.RSA_SHA1, null);
            } else {
                throw new RuntimeException("Error signing SAML element: Unsupported type of key");
            }

            final CanonicalizationMethod canonicalizationMethod = sigFactory
                    .newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                            (C14NMethodParameterSpec) null);

            // Create the SignedInfo
            final SignedInfo signedInfo = sigFactory.newSignedInfo(
                    canonicalizationMethod, signatureMethod, Collections
                            .singletonList(ref));

            // Create a KeyValue containing the DSA or RSA PublicKey
            final KeyInfoFactory keyInfoFactory = sigFactory
                    .getKeyInfoFactory();
            final KeyValue keyValuePair = keyInfoFactory.newKeyValue(pubKey);

            // Create a KeyInfo and add the KeyValue to it
            final KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections
                    .singletonList(keyValuePair));
            // Convert the JDOM document to w3c (Java XML signature API requires
            // w3c representation)
            final org.w3c.dom.Element w3cElement = toDom(element);

            // Create a DOMSignContext and specify the DSA/RSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            final DOMSignContext dsc = new DOMSignContext(privKey, w3cElement);

            final org.w3c.dom.Node xmlSigInsertionPoint = getXmlSignatureInsertLocation(w3cElement);
            dsc.setNextSibling(xmlSigInsertionPoint);

            // Marshal, generate (and sign) the enveloped signature
            final XMLSignature signature = sigFactory.newXMLSignature(signedInfo,
                    keyInfo);
            signature.sign(dsc);

            return toJdom(w3cElement);

        } catch (final Exception e) {
            throw new RuntimeException("Error signing SAML element: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the xml signature insert location.
     *
     * @param elem the elem
     * @return the xml signature insert location
     */
    private static Node getXmlSignatureInsertLocation(final org.w3c.dom.Element elem) {
        org.w3c.dom.Node insertLocation = null;
        org.w3c.dom.NodeList nodeList = elem.getElementsByTagNameNS(
                SAMLConstants.SAML20P_NS, "Extensions");
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
    private org.w3c.dom.Element toDom(final org.jdom.Element element) {
        return toDom(element.getDocument()).getDocumentElement();
    }

    /**
     * Convert the received jdom doc to a Document element.
     *
     * @param doc the doc
     * @return the org.w3c.dom. document
     */
    private org.w3c.dom.Document toDom(final Document doc) {
        try {
            final XMLOutputter xmlOutputter = new XMLOutputter();
            final StringWriter elemStrWriter = new StringWriter();
            xmlOutputter.output(doc, elemStrWriter);
            final byte[] xmlBytes = elemStrWriter.toString().getBytes(Charset.defaultCharset());
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xmlBytes));
        } catch (final Exception e) {
            logger.trace(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert to a jdom element.
     *
     * @param e the e
     * @return the element
     */
    private static org.jdom.Element toJdom(final org.w3c.dom.Element e) {
        return  new DOMBuilder().build(e);
    }
}

