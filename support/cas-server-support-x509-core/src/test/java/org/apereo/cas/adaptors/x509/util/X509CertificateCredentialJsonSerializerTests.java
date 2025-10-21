package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509CertificateCredentialJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("X509")
class X509CertificateCredentialJsonSerializerTests {
    private static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() {
        val credential = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        val json = MAPPER.writeValueAsString(credential);
        assertNotNull(json);
        val obj = MAPPER.readValue(json, new TypeReference<X509CertificateCredential>() {
        });
        assertNotNull(obj);
        val serializer = new X509CertificateCredentialJsonSerializer();
        assertSame(X509CertificateCredential.class, serializer.handledType());
    }

    @Test
    void verifyAuditableOperation() {
        val credential = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        val set = new LinkedHashSet<>();
        set.add(credential);
        val json = AuditTrailManager.toJson(set);
        assertNotNull(json);
    }
}
