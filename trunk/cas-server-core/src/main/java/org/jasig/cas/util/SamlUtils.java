/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
import java.util.TimeZone;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class SamlUtils {

    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    private static final String JSR_105_PROVIDER = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";

    private SamlUtils() {
        // nothing to do
    }

    public static String getCurrentDateAndTime() {
        return getFormattedDateAndTime(new Date());
    }

    public static String getFormattedDateAndTime(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat.format(date);
    }

    public static String signSamlResponse(final String samlResponse,
        final PrivateKey privateKey, final PublicKey publicKey) {
        final Document doc = constructDocumentFromXmlString(samlResponse);

        if (doc != null) {
            final Element signedElement = signSamlElement((Element) doc
                .getChildNodes().item(0), privateKey, publicKey);

            try {
                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer trans = tf.newTransformer();
                final StringWriter sw = new StringWriter();
                trans.transform(new DOMSource(signedElement), new StreamResult(
                    sw));
                return sw.toString();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

        }
        throw new RuntimeException("Error signing SAML Response: Null document");
    }

    public static Document constructDocumentFromXmlString(final String xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes()));
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

            final DOMSignContext dsc = new DOMSignContext(privKey, element);

            final Node xmlSigInsertionPoint = getXmlSignatureInsertLocation(element);
            dsc.setNextSibling(xmlSigInsertionPoint);

            // Marshal, generate (and sign) the enveloped signature
            final XMLSignature signature = sigFactory.newXMLSignature(
                signedInfo, keyInfo);
            signature.sign(dsc);

            return element;

        } catch (final Exception e) {
            throw new RuntimeException("Error signing SAML element: "
                + e.getMessage(), e);
        }
    }

    private static Node getXmlSignatureInsertLocation(Element elem) {
        return elem.getElementsByTagName("samlp:Status").item(0);
    }
}
