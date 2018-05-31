package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.util.crypto.CertUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;

import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link X509CommonNameEDIPIPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class X509CommonNameEDIPIPrincipalResolverTests extends AbstractX509CertificateTests {
    private final X509CommonNameEDIPIPrincipalResolver resolver = new X509CommonNameEDIPIPrincipalResolver();

    @Test
    public void verifyResolvePrincipalInternal() throws Exception {
        try (var is = new ClassPathResource("edipi.cer").getInputStream()) {
            final InputStreamSource iso = new InputStreamResource(is);
            final var cert = CertUtils.readCertificate(iso);
            final var c = new X509CertificateCredential(new X509Certificate[]{cert});
            c.setCertificate(cert);
            final var principal = this.resolver.resolve(c, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
            assertEquals("1234567890", principal.getId());
        }
    }
}
