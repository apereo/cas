package org.apereo.cas.support.saml.idp.metadata.writer;

import org.apereo.cas.util.RandomUtils;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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

import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This is {@link DefaultSamlIdPCertificateAndKeyWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor
@Setter
public class DefaultSamlIdPCertificateAndKeyWriter implements SamlIdPCertificateAndKeyWriter {
    private static final int X509_CERT_BITS_SIZE = 160;

    private int keySize = 2048;
    private String hostname;
    private String keyType = "RSA";
    private String certificateAlgorithm = "SHA256withRSA";
    private int certificateLifetimeInYears = 20;
    private List<String> uriSubjectAltNames;

    @SneakyThrows
    @Override
    public void writeCertificateAndKey(final Writer privateKeyWriter, final Writer certificateWriter) {
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
    }

    @SneakyThrows
    private KeyPair generateKeyPair() {
        val generator = KeyPairGenerator.getInstance(keyType);
        generator.initialize(keySize);
        return generator.generateKeyPair();
    }

    private X509Certificate generateCertificate(final KeyPair keypair) throws Exception {
        val dn = new X500Name("CN=" + hostname);
        val notBefore = new GregorianCalendar();
        val notOnOrAfter = new GregorianCalendar();
        notOnOrAfter.set(GregorianCalendar.YEAR, notOnOrAfter.get(GregorianCalendar.YEAR) + certificateLifetimeInYears);

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
