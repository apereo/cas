package org.apereo.cas.adaptors.x509;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0
 */
@SpringBootTest(
    classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.x509.principal-type=SUBJECT_DN",
        "cas.authn.attribute-repository.groovy[0].location=classpath:/GroovyAttributeDao.groovy",
        "cas.authn.attribute-repository.groovy[0].order=1",
        "cas.authn.attribute-repository.core.merger=MULTIVALUED"
    })
@Tag("X509")
public class X509SubjectDNPrincipalResolverAggregateTests {
    private static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    @Autowired
    @Qualifier("casAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Test
    public void verifyResolverAsAggregate() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        val result = authenticationManager.authenticate(
            new DefaultAuthenticationTransactionFactory().newTransaction(c));
        assertNotNull(result);
        val attributes = result.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("subjectX500Principal"));
        assertTrue(attributes.containsKey("groovySubjectX500Principal"));
    }

}
