/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import javax.xml.parsers.DocumentBuilderFactory;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Node;

/**
 * Utilities adopted from the Google sample code.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class SamlUtils {

    private static final String JSR_105_PROVIDER = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";

    private static final String SAML_PROTOCOL_NS_URI_V20 = "urn:oasis:names:tc:SAML:2.0:protocol";

    private SamlUtils() {
        // nothing to do
    }

    public static String getCurrentDateAndTime() {
        return getFormattedDateAndTime(new Date());
    }

    public static String getFormattedDateAndTime(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
        // Google Does not set this.
        // dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat.format(date);
    }

    public static String signSamlResponse(final String samlResponse,
        final PrivateKey privateKey, final PublicKey publicKey) {
        final Document doc = constructDocumentFromXmlString(samlResponse);

        if (doc != null) {
            final Element signedElement = signSamlElement(doc.getRootElement(),
                privateKey, publicKey);
            doc.setRootElement((Element) signedElement.detach());
            return new XMLOutputter().outputString(doc);
        }
        throw new RuntimeException("Error signing SAML Response: Null document");
    }

    public static Document constructDocumentFromXmlString(String xmlString) {
        try {
            final SAXBuilder builder = new SAXBuilder();
            builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return builder
                .build(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (final Exception e) {
            return null;
        }
    }

    private static Element signSamlElement(Element element, PrivateKey privKey,
        PublicKey pubKey) {
        try {
            final String providerName = System.getProperty("jsr105Provider",
                JSR_105_PROVIDER);
            final XMLSignatureFactory sigFactory = XMLSignatureFactory
                .getInstance("DOM", (Provider) Class.forName(providerName)
                    .newInstance());

            final List envelopedTransform = Collections
                .singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                    (TransformParameterSpec) null));

            final Reference ref = sigFactory.newReference("", sigFactory
                .newDigestMethod(DigestMethod.SHA1, null), envelopedTransform,
                null, null);

            // Create the SignatureMethod based on the type of key
            SignatureMethod signatureMethod;
            if (pubKey instanceof DSAPublicKey) {
                signatureMethod = sigFactory.newSignatureMethod(
                    SignatureMethod.DSA_SHA1, null);
            } else if (pubKey instanceof RSAPublicKey) {
                signatureMethod = sigFactory.newSignatureMethod(
                    SignatureMethod.RSA_SHA1, null);
            } else {
                throw new RuntimeException(
                    "Error signing SAML element: Unsupported type of key");
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
            // w3c
            // representation)
            org.w3c.dom.Element w3cElement = toDom(element);

            // Create a DOMSignContext and specify the DSA/RSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            DOMSignContext dsc = new DOMSignContext(privKey, w3cElement);

            org.w3c.dom.Node xmlSigInsertionPoint = getXmlSignatureInsertLocation(w3cElement);
            dsc.setNextSibling(xmlSigInsertionPoint);

            // Marshal, generate (and sign) the enveloped signature
            XMLSignature signature = sigFactory.newXMLSignature(signedInfo,
                keyInfo);
            signature.sign(dsc);

            return toJdom(w3cElement);

        } catch (final Exception e) {
            throw new RuntimeException("Error signing SAML element: "
                + e.getMessage(), e);
        }
    }

    private static Node getXmlSignatureInsertLocation(org.w3c.dom.Element elem) {
        org.w3c.dom.Node insertLocation = null;
        org.w3c.dom.NodeList nodeList = elem.getElementsByTagNameNS(
            SAML_PROTOCOL_NS_URI_V20, "Extensions");
        if (nodeList.getLength() != 0) {
            insertLocation = nodeList.item(nodeList.getLength() - 1);
        } else {
            nodeList = elem.getElementsByTagNameNS(SAML_PROTOCOL_NS_URI_V20,
                "Status");
            insertLocation = nodeList.item(nodeList.getLength() - 1);
        }
        return insertLocation;
    }

    private static org.w3c.dom.Element toDom(final Element element) {
        return toDom(element.getDocument()).getDocumentElement();
    }

    private static org.w3c.dom.Document toDom(final Document doc) {
        try {
            final XMLOutputter xmlOutputter = new XMLOutputter();
            final StringWriter elemStrWriter = new StringWriter();
            xmlOutputter.output(doc, elemStrWriter);
            final byte[] xmlBytes = elemStrWriter.toString().getBytes();
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                .newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().parse(
                new ByteArrayInputStream(xmlBytes));
        } catch (final Exception e) {
            return null;
        }
    }
    
    private static Element toJdom(final org.w3c.dom.Element e) {
        return  new DOMBuilder().build(e);
      }
}
