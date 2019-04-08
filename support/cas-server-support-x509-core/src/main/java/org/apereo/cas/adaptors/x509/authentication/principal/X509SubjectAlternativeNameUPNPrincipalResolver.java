package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.common.base.Predicates;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

/**
 * Credential to principal resolver that extracts Subject Alternative Name UPN extension
 * from the provided certificate if available as a resolved principal id.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public class X509SubjectAlternativeNameUPNPrincipalResolver extends AbstractX509PrincipalResolver {

    /**
     * ObjectID for upn altName for windows smart card logon.
     */
    public static final String UPN_OBJECTID = "1.3.6.1.4.1.311.20.2.3";

    public X509SubjectAlternativeNameUPNPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                          final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                                          final String principalAttributeName,
                                                          final String alternatePrincipalAttribute,
                                                          final boolean useCurrentPrincipalId,
                                                          final boolean resolveAttributes,
                                                          final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, alternatePrincipalAttribute, useCurrentPrincipalId,
            resolveAttributes, activeAttributeRepositoryIdentifiers);
    }

    /**
     * Get UPN String.
     *
     * @param seq ASN1Sequence abstraction representing subject alternative name.
     *            First element is the object identifier, second is the object itself.
     * @return UPN string or null
     */
    private static String getUPNStringFromSequence(final ASN1Sequence seq) {
        if (seq == null) {
            return null;
        }
        val id = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
        if (id != null && UPN_OBJECTID.equals(id.getId())) {
            val obj = (ASN1TaggedObject) seq.getObjectAt(1);
            val primitiveObj = obj.getObject();

            val func = FunctionUtils.doIf(Predicates.instanceOf(ASN1TaggedObject.class),
                () -> ASN1TaggedObject.getInstance(primitiveObj).getObject(),
                () -> primitiveObj);
            val prim = func.apply(primitiveObj);

            if (prim instanceof ASN1OctetString) {
                return new String(((ASN1OctetString) prim).getOctets(), StandardCharsets.UTF_8);
            }
            if (prim instanceof ASN1String) {
                return ((ASN1String) prim).getString();
            }
        }
        return null;
    }

    /**
     * Get alt name seq.
     *
     * @param sanItem subject alternative name value encoded as a two elements List with elem(0) representing object id and elem(1)
     *                representing object (subject alternative name) itself.
     * @return ASN1Sequence abstraction representing subject alternative name or null if the passed in
     * List doesn't contain at least two elements.
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private static ASN1Sequence getAltnameSequence(final List sanItem) {
        //Should not be the case, but still, a extra "safety" check
        if (sanItem.size() < 2) {
            LOGGER.error("Subject Alternative Name List does not contain at least two required elements. Returning null principal id...");
        }
        val itemType = (Integer) sanItem.get(0);
        if (itemType == 0) {
            val altName = (byte[]) sanItem.get(1);
            return getAltnameSequence(altName);
        }
        return null;
    }

    /**
     * Get alt name seq.
     *
     * @param sanValue subject alternative name value encoded as byte[]
     * @return ASN1Sequence abstraction representing subject alternative name
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private static ASN1Sequence getAltnameSequence(final byte[] sanValue) {
        try (val bInput = new ByteArrayInputStream(sanValue);
             val input = new ASN1InputStream(bInput)) {
            return ASN1Sequence.getInstance(input.readObject());
        } catch (final IOException e) {
            LOGGER.error("An error has occurred while reading the subject alternative name value", e);
        }
        return null;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name UPN for [{}]", certificate);
        try {
            val subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                for (val sanItem : subjectAltNames) {
                    val seq = getAltnameSequence(sanItem);
                    val upnString = getUPNStringFromSequence(seq);
                    if (upnString != null) {
                        return upnString;
                    }
                }
            }
        } catch (final CertificateParsingException e) {
            LOGGER.error("Error is encountered while trying to retrieve subject alternative names collection from certificate", e);
            return getAlternatePrincipal(certificate);
        }

        return getAlternatePrincipal(certificate);
    }
}
