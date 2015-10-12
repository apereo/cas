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
package org.jasig.cas.adaptors.x509.authentication.principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

/**
 * Credential to principal resolver that extracts Subject Alternative Name UPN extension
 * from the provided certificate if available as a resolved principal id.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
public class X509SubjectAlternativeNameUPNPrincipalResolver extends AbstractX509PrincipalResolver {

    /**
     * ObjectID for upn altName for windows smart card logon.
     */
    public static final String UPN_OBJECTID = "1.3.6.1.4.1.311.20.2.3";

    /**
     * Retrieves Subject Alternative Name UPN extension as a principal id String.
     *
     * @param certificate X.509 certificate credential.
     *
     * @return Resolved principal ID or null if no SAN UPN extension is available in provided certificate.
     *
     * @see AbstractX509PrincipalResolver#resolvePrincipalInternal(java.security.cert.X509Certificate)
     * @see java.security.cert.X509Certificate#getSubjectAlternativeNames()
     */
    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        logger.debug("Resolving principal from Subject Alternative Name UPN for {}", certificate);
        try {
            final Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                for (final List<?> sanItem : subjectAltNames) {
                    final ASN1Sequence seq = getAltnameSequence(sanItem);
                    final String upnString = getUPNStringFromSequence(seq);
                    if (upnString != null) {
                        return upnString;
                    }
                }
            }
        } catch (final CertificateParsingException e) {
            logger.error("Error is encountered while trying to retrieve subject alternative names collection from certificate", e);
            logger.debug("Returning null principal id...");
            return null;
        }
        logger.debug("Returning null principal id...");
        return null;
    }

    /**
     * Get UPN String.
     *
     * @param seq ASN1Sequence abstraction representing subject alternative name.
     * First element is the object identifier, second is the object itself.
     *
     * @return UPN string or null
     */
    private String getUPNStringFromSequence(final ASN1Sequence seq) {
        if (seq != null) {
            // First in sequence is the object identifier, that we must check
            final ASN1ObjectIdentifier id = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
            if (id != null && UPN_OBJECTID.equals(id.getId())) {
                final ASN1TaggedObject obj = (ASN1TaggedObject) seq.getObjectAt(1);
                ASN1Primitive prim = obj.getObject();
                
                // Due to bug in java cert.getSubjectAltName, it can be tagged an extra time
                if (prim instanceof ASN1TaggedObject) {
                    prim = ASN1TaggedObject.getInstance(((ASN1TaggedObject) prim)).getObject();
                }

                if (prim instanceof ASN1OctetString) {
                    return new String(((ASN1OctetString) prim).getOctets());
                } else if (prim instanceof ASN1String) {
                    return ((ASN1String) prim).getString();
                } else{
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Get alt name seq.
     *
     * @param sanItem subject alternative name value encoded as a two elements List with elem(0) representing object id and elem(1)
     * representing object (subject alternative name) itself.
     *
     * @return ASN1Sequence abstraction representing subject alternative name or null if the passed in
     * List doesn't contain at least to elements
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     *     X509Certificate#getSubjectAlternativeNames</a>
     */
    private ASN1Sequence getAltnameSequence(final List sanItem) {
        //Should not be the case, but still, a extra "safety" check
        if (sanItem.size() < 2) {
            logger.error("Subject Alternative Name List does not contain at least two required elements. Returning null principal id...");
        }
        final Integer itemType = (Integer) sanItem.get(0);
        if (itemType == 0) {
            final byte[] altName = (byte[]) sanItem.get(1);
            return getAltnameSequence(altName);
        }
        return null;
    }

    /**
     * Get alt name seq.
     *
     * @param sanValue subject alternative name value encoded as byte[]
     *
     * @return ASN1Sequence abstraction representing subject alternative name
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     *     X509Certificate#getSubjectAlternativeNames</a>
     */
    private ASN1Sequence getAltnameSequence(final byte[] sanValue) {
        ASN1Primitive oct = null;
        try (final ByteArrayInputStream bInput = new ByteArrayInputStream(sanValue)) {
            try (final ASN1InputStream input = new ASN1InputStream(bInput)) {
                oct = input.readObject();
            } catch (final IOException e) {
                logger.error("Error on getting Alt Name as a DERSEquence: {}", e.getMessage(), e);
            }
            return ASN1Sequence.getInstance(oct);
        } catch (final IOException e) {
            logger.error("An error has occurred while reading the subject alternative name value", e);
        }
        return null;
    }
}
