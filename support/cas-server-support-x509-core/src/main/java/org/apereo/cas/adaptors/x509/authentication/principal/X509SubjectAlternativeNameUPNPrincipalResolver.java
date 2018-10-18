package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                                                          final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    /**
     * Retrieves Subject Alternative Name UPN extension as a principal id String.
     *
     * @param certificate X.509 certificate credential.
     * @return Resolved principal ID or null if no SAN UPN extension is available in provided certificate.
     * @see java.security.cert.X509Certificate#getSubjectAlternativeNames()
     */
    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name UPN for [{}]", certificate);
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
            LOGGER.error("Error is encountered while trying to retrieve subject alternative names collection from certificate", e);
            LOGGER.debug("Returning null principal...");
            return null;
        }
        LOGGER.debug("Returning null principal id...");
        return null;
    }

    /**
     * Get UPN String.
     *
     * @param seq ASN1Sequence abstraction representing subject alternative name.
     *            First element is the object identifier, second is the object itself.
     * @return UPN string or null
     */
    private static String getUPNStringFromSequence(final ASN1Sequence seq) {
        if (seq != null) {
            // First in sequence is the object identifier, that we must check
            final ASN1ObjectIdentifier id = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
            if (id != null && UPN_OBJECTID.equals(id.getId())) {
                final ASN1TaggedObject obj = (ASN1TaggedObject) seq.getObjectAt(1);
                ASN1Primitive prim = obj.getObject();
                // Due to bug in java cert.getSubjectAltName, it can be tagged an extra time
                if (prim instanceof ASN1TaggedObject) {
                    prim = ASN1TaggedObject.getInstance(prim).getObject();
                }
                if (prim instanceof ASN1OctetString) {
                    return new String(((ASN1OctetString) prim).getOctets(), StandardCharsets.UTF_8);
                }
                if (prim instanceof ASN1String) {
                    return ((ASN1String) prim).getString();
                }
                return null;
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
     * List doesn't contain at least to elements
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private static ASN1Sequence getAltnameSequence(final List sanItem) {
        //Should not be the case, but still, a extra "safety" check
        if (sanItem.size() < 2) {
            LOGGER.error("Subject Alternative Name List does not contain at least two required elements. Returning null principal id...");
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
     * @return ASN1Sequence abstraction representing subject alternative name
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private static ASN1Sequence getAltnameSequence(final byte[] sanValue) {
        ASN1Primitive oct = null;
        try (ByteArrayInputStream bInput = new ByteArrayInputStream(sanValue)) {
            try (ASN1InputStream input = new ASN1InputStream(bInput)) {
                oct = input.readObject();
            } catch (final IOException e) {
                LOGGER.error("Error on getting Alt Name as a DERSEquence: [{}]", e.getMessage(), e);
            }
            return ASN1Sequence.getInstance(oct);
        } catch (final IOException e) {
            LOGGER.error("An error has occurred while reading the subject alternative name value", e);
        }
        return null;
    }

    @Override
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential) {
        final Map<String, List<Object>> attributes = new LinkedHashMap<>(super.retrievePersonAttributes(principalId, credential));
        final X509Certificate certificate = ((X509CertificateCredential) credential).getCertificate();

        if (certificate != null) {
            if (StringUtils.isNotBlank(certificate.getSigAlgOID())) {
                attributes.put("sigAlgOid", CollectionUtils.wrapList(certificate.getSigAlgOID()));
            }
            final Principal subjectDn = certificate.getSubjectDN();
            if (subjectDn != null) {
                attributes.put("subjectDn", CollectionUtils.wrapList(subjectDn.getName()));
            }
            final Principal subjectPrincipal = certificate.getSubjectX500Principal();
            if (subjectPrincipal != null) {
                attributes.put("subjectX500Principal", CollectionUtils.wrapList(subjectPrincipal.getName()));
            }
        }
        return attributes;
    }
}
