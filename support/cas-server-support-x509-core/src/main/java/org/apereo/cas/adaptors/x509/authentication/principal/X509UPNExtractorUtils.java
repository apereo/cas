package org.apereo.cas.adaptors.x509.authentication.principal;


import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Credential to principal resolver that extracts Subject Alternative Name UPN extension
 * from the provided certificate if available as a resolved principal id.
 *
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 4.1.0
 */
@Slf4j
@UtilityClass
public class X509UPNExtractorUtils {

    /**
     * ObjectID for upn altName for windows smart card logon.
     */
    private static final String UPN_OBJECTID = "1.3.6.1.4.1.311.20.2.3";

    /**
     * Integer representing the type of the subject alt name known as OtherName or ANY.
     */
    private static final int SAN_TYPE_OTHER = 0;

    /**
     * Get UPN String.
     *
     * @param seq ASN1Sequence abstraction representing subject alternative name.
     *            First element is the object identifier, second is the object itself.
     * @return UPN string or null
     */
    private String getUPNStringFromSequence(final ASN1Sequence seq) {
        val id = Optional.ofNullable(seq).map(asn1Encodables -> ASN1ObjectIdentifier.getInstance(asn1Encodables.getObjectAt(0))).orElse(null);
        if (id != null && UPN_OBJECTID.equals(id.getId())) {
            val obj = (ASN1TaggedObject) seq.getObjectAt(1);
            val primitiveObj = obj.getBaseObject();

            val func = FunctionUtils.doIf(Predicates.instanceOf(ASN1TaggedObject.class),
                () -> ASN1TaggedObject.getInstance(primitiveObj).getBaseObject(),
                () -> primitiveObj);
            val prim = func.apply(primitiveObj);

            if (prim instanceof final ASN1OctetString instance) {
                return new String(instance.getOctets(), StandardCharsets.UTF_8);
            }
            if (prim instanceof final ASN1String instance) {
                return instance.getString();
            }
        }
        return null;
    }

    /**
     * Get the alt name sequence if it is of type OtherName, otherwise return null.
     *
     * @param sanItem subject alternative name value encoded as a two elements List with elem(0) representing object id and elem(1)
     *                representing object (subject alternative name) itself.
     * @return ASN1Sequence abstraction representing subject alternative name or null if the passed in
     * List doesn't contain at least two elements and the type of the element is not the type OtherName.
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private ASN1Sequence getOtherNameTypeSAN(final List<?> sanItem) {
        if (sanItem.size() < 2) {
            LOGGER.error("Subject Alternative Name List does not contain at least two required elements");
            return null;
        }
        val itemType = (Integer) sanItem.getFirst();
        if (itemType == SAN_TYPE_OTHER) {
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
     * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    private ASN1Sequence getAltnameSequence(final byte[] sanValue) {
        try (val bInput = new ByteArrayInputStream(sanValue);
             val input = new ASN1InputStream(bInput)) {
            return ASN1Sequence.getInstance(input.readObject());
        } catch (final IOException e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Return the first {@code X509UPNExtractorUtils.UPN_OBJECTID} found in the subject alternative names (SAN) extension field of the certificate.
     *
     * @param subjectAltNames X509 certificate subject alt names
     * @return User principal name, or null if no SAN found matching UPN type.
     */
    public Optional<String> extractUPNString(final Collection<List<?>> subjectAltNames) {
        for (val sanItem : subjectAltNames) {
            if (LOGGER.isTraceEnabled()) {
                if (sanItem.size() == 2) {
                    val name = sanItem.get(1);
                    val value = name instanceof String ? name : name instanceof final byte[] array ? getAltnameSequence(array) : name;
                    LOGGER.trace("Found subject alt name of type [{}] with value [{}]", sanItem.getFirst(), value);
                } else {
                    LOGGER.trace("SAN item of unexpected size found: [{}]", sanItem);
                }
            }
            val seq = getOtherNameTypeSAN(sanItem);
            val upnString = getUPNStringFromSequence(seq);
            if (StringUtils.isNotBlank(upnString)) {
                LOGGER.debug("Found user principal name in certificate: [{}]", upnString);
                return Optional.of(upnString);
            }
        }
        return Optional.empty();
    }
}
