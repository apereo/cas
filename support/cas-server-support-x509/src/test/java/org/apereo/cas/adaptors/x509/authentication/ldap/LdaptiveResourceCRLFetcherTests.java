package org.apereo.cas.adaptors.x509.authentication.ldap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.AbstractX509LdapTests;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.adaptors.x509.util.CertUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.cert.X509Certificate;


/**
 * Test cases for {@link LdaptiveResourceCRLFetcher}
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {X509AuthenticationConfiguration.class, 
        RefreshAutoConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class})
@TestPropertySource(locations = {"classpath:/x509.properties"})
public class LdaptiveResourceCRLFetcherTests extends AbstractX509LdapTests {
    
    @Autowired
    @Qualifier("crlFetcher")
    private CRLFetcher fetcher;

    @BeforeClass
    public static void bootstrap() throws Exception {
        AbstractX509LdapTests.bootstrap();
    }

    @Test
    public void getCrlFromLdap() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);

        for (int i = 0; i < 10; i++) {
            final CRLDistributionPointRevocationChecker checker = 
                    new CRLDistributionPointRevocationChecker(false, new AllowRevocationPolicy(), null,
                            cache, fetcher, true);
            final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }
    }

    @Test
    public void getCrlFromLdapWithNoCaching() throws Exception {
        for (int i = 0; i < 10; i++) {
            CacheManager.getInstance().removeAllCaches();
            final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
            CacheManager.getInstance().addCache(cache);
            final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(
                    false, new AllowRevocationPolicy(), null,
                    cache, fetcher, true);
            final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }
    }

}
