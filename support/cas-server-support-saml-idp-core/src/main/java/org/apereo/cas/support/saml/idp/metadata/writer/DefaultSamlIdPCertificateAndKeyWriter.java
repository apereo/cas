package org.apereo.cas.support.saml.idp.metadata.writer;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import java.security.cert.X509Certificate;

/**
 * This is {@link DefaultSamlIdPCertificateAndKeyWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Setter
public class DefaultSamlIdPCertificateAndKeyWriter implements SamlIdPCertificateAndKeyWriter {
    private static final int X509_CERT_BITS_SIZE = 160;

    private final String hostname;

    private int keySize = 4096;

    private String keyType = "RSA";

    private String certificateAlgorithm = "SHA512withRSA";

    private int certificateLifetimeInYears = 20;

    private List<String> uriSubjectAltNames;

    @Override
    public void writeCertificateAndKey(final Writer privateKeyWriter, final Writer certificateWriter) {
        FunctionUtils.doUnchecked(_ -> {
            val keypair = generateKeyPair();
            val certificate = generateCertificate(keypair);
            try (val keyOut = new JcaPEMWriter(privateKeyWriter)) {
                keyOut.writeObject(keypair.getPrivate());
                keyOut.flush();
            }

            try (val certOut = new JcaPEMWriter(certificateWriter)) {
                certOut.writeObject(certificate);
                certOut.flush();
            }
        });
    }

    private KeyPair generateKeyPair() {
        return FunctionUtils.doUnchecked(() -> {
            val generator = KeyPairGenerator.getInstance(keyType);
            generator.initialize(keySize);
            return generator.generateKeyPair();
        });
    }

    private X509Certificate generateCertificate(final KeyPair keypair) throws Exception {
        val dn = new X500Name("CN=" + hostname);
        val notBefore = new GregorianCalendar();
        val notOnOrAfter = new GregorianCalendar();
        notOnOrAfter.set(Calendar.YEAR, notOnOrAfter.get(Calendar.YEAR) + certificateLifetimeInYears);

        val builder = new JcaX509v3CertificateBuilder(
            dn,
            new BigInteger(X509_CERT_BITS_SIZE, RandomUtils.getNativeInstance()),
            notBefore.getTime(),
            notOnOrAfter.getTime(),
            dn,
            keypair.getPublic()
        );

        val extUtils = new JcaX509ExtensionUtils();
        builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(keypair.getPublic()));
        builder.addExtension(Extension.subjectAlternativeName, false, GeneralNames.getInstance(new DERSequence(buildSubjectAltNames())));

        val certHldr = builder.build(new JcaContentSignerBuilder(certificateAlgorithm).build(keypair.getPrivate()));
        val cert = new JcaX509CertificateConverter().getCertificate(certHldr);
        cert.checkValidity(new Date());
        cert.verify(keypair.getPublic());

        return cert;
    }

    private ASN1Encodable[] buildSubjectAltNames() {
        val subjectAltNames = new ArrayList<ASN1Encodable>(2);
        subjectAltNames.add(new GeneralName(GeneralName.dNSName, hostname));

        if (uriSubjectAltNames != null) {
            uriSubjectAltNames.forEach(subjectAltName -> subjectAltNames.add(new GeneralName(GeneralName.uniformResourceIdentifier, subjectAltName)));
        }
        return subjectAltNames.toArray(ASN1Encodable[]::new);
    }
}
