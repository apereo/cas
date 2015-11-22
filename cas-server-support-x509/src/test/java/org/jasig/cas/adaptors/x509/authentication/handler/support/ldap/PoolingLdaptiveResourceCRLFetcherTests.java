package org.jasig.cas.adaptors.x509.authentication.handler.support.ldap;

import java.security.cert.X509Certificate;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.jasig.cas.adaptors.x509.authentication.handler.support.AbstractX509LdapTests;
import org.jasig.cas.adaptors.x509.authentication.handler.support.AllowRevocationPolicy;
import org.jasig.cas.adaptors.x509.authentication.handler.support.CRLDistributionPointRevocationChecker;
import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Test cases for {@link PoolingLdaptiveResourceCRLFetcher}
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/x509-ldap-context.xml"})
public class PoolingLdaptiveResourceCRLFetcherTests extends AbstractX509LdapTests {


    @Autowired
    @Qualifier("poolingLdapCertFetcher")
    private PoolingLdaptiveResourceCRLFetcher fetcher;

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
            final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
            checker.setThrowOnFetchFailure(true);
            checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
            final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.init();
            checker.check(cert);
        }
    }

    @Test
    public void getCrlFromLdapWithNoCaching() throws Exception {
        for (int i = 0; i < 10; i++) {
            CacheManager.getInstance().removeAllCaches();
            final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
            CacheManager.getInstance().addCache(cache);
            final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
            checker.setThrowOnFetchFailure(true);
            checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
            final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.init();
            checker.check(cert);
        }
    }

}
