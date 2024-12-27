package org.apereo.cas.adaptors.x509.authentication.ldap;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LdapTestUtils;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link LdaptiveResourceCRLFetcher}
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Ldap")
@EnabledIfListeningOnPort(port = 10389)
class LdaptiveResourceCRLFetcherTests {

    private static final String DN = "CN=x509,ou=people,dc=example,dc=org";

    @BeforeAll
    public static void bootstrap() throws Exception {
        @Cleanup
        val localhost = new LDAPConnection("localhost",
            BaseX509LdapResourceFetcherTests.LDAP_PORT, "cn=Directory Manager", "password");
        val ldif = new ClassPathResource("ldap-x509.ldif");
        LdapIntegrationTestsOperations.populateEntries(localhost, ldif.getInputStream(), "ou=people,dc=example,dc=org");

        val userCA = new byte[1024];
        IOUtils.read(new ClassPathResource("userCA-valid.crl").getInputStream(), userCA);
        val value = EncodingUtils.encodeBase64ToByteArray(userCA);
        val attr = LdapAttribute.builder().name("certificateRevocationList").values(value).binary(true).build();

        val bindInit = new BindConnectionInitializer("cn=Directory Manager", new Credential("password"));
        LdapTestUtils.modifyLdapEntry(localhost, DN, attr, bindInit);
    }

    @Test
    void verifyOperation() throws Throwable {
        val config = mock(ConnectionConfig.class);
        val operation = mock(SearchOperation.class);
        val fetcher = new LdaptiveResourceCRLFetcher(config, operation, "attribute");
        assertNull(fetcher.fetch(new URI("https://github.com")));
        assertNull(fetcher.fetch(new URI("https://github.com").toURL()));
        assertNull(fetcher.fetch("https://github.com"));
    }

    @Nested
    @Tag("Ldap")
    @TestPropertySource(properties = "cas.authn.x509.ldap.certificate-attribute=cn")
    class InvalidNonBinaryAttributeFetchFromLdap extends BaseX509LdapResourceFetcherTests {
        @Autowired
        @Qualifier("crlFetcher")
        private CRLFetcher fetcher;

        @Test
        void verifyResourceFromResourceUrl() {
            val resource = mock(Resource.class);
            when(resource.toString()).thenReturn("ldap://localhost:10389");
            assertThrows(CertificateException.class, () -> fetcher.fetch(resource));
        }
    }

    @Nested
    @Tag("Ldap")
    @TestPropertySource(properties = "cas.authn.x509.ldap.certificate-attribute=unknown")
    class UnknownAttributeFetchFromLdap extends BaseX509LdapResourceFetcherTests {
        @Autowired
        @Qualifier("crlFetcher")
        private CRLFetcher fetcher;

        @Test
        void verifyResourceFromResourceUrl() {
            val resource = mock(Resource.class);
            when(resource.toString()).thenReturn("ldap://localhost:10389");
            assertThrows(CertificateException.class, () -> fetcher.fetch(resource));
        }
    }

    @Nested
    @Tag("Ldap")
    class DefaultFetchFromLdap extends BaseX509LdapResourceFetcherTests {
        @Autowired
        @Qualifier("crlFetcher")
        private CRLFetcher fetcher;

        @Test
        void verifyResourceFromResourceUrl() throws Throwable {
            val resource = mock(Resource.class);
            when(resource.toString()).thenReturn("ldap://localhost:10389");
            assertNotNull(fetcher.fetch(resource));

            val uri = new URI("ldap://localhost:1389");
            assertNotNull(fetcher.fetch(uri));

            val url = mock(URL.class);
            when(url.toString()).thenReturn("ldap://localhost:10389");
            when(url.getProtocol()).thenReturn("ldap");
            assertNotNull(fetcher.fetch(url));

            assertNotNull(fetcher.fetch("ldap://localhost:10389"));
        }

        @Test
        void getCrlFromLdap() throws Exception {
            val cache = getCache();
            for (var i = 0; i < 10; i++) {
                val checker =
                    new CRLDistributionPointRevocationChecker(false, new AllowRevocationPolicy(), null,
                        cache, fetcher, true);
                val cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
                checker.check(cert);
            }
        }

        @Test
        void getCrlFromLdapWithNoCaching() throws Exception {
            for (var i = 0; i < 10; i++) {
                val cache = getCache();
                val checker = new CRLDistributionPointRevocationChecker(
                    false, new AllowRevocationPolicy(), null,
                    cache, fetcher, true);
                val cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
                checker.check(cert);
            }
        }

        private static Cache<URI, byte[]> getCache() {
            return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Beans.newDuration("PT1H"))
                .build();
        }
    }
}
