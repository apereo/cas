package org.apereo.cas.adaptors.x509.authentication.ldap;

import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.AbstractX509LdapTests;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;


/**
 * Test cases for {@link LdaptiveResourceCRLFetcher}
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@SpringBootTest(classes = {
    X509AuthenticationConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class
})
@TestPropertySource(properties = {
    "ldap.managerDn=cn=Directory Manager,dc=example,dc=org",
    "ldap.managerPassword=Password",
    "cas.authn.attributeRepository.stub.attributes.uid=uid",
    "cas.authn.attributeRepository.stub.attributes.eduPersonAffiliation=developer",
    "cas.authn.attributeRepository.stub.attributes.groupMembership=adopters",
    "cas.authn.attributeRepository.stub.attributes.certificateRevocationList=certificateRevocationList",
    "cas.authn.x509.regExTrustedIssuerDnPattern=CN=\\w+,DC=jasig,DC=org",
    "cas.authn.x509.principalType=SERIAL_NO_DN",
    "cas.authn.policy.any.tryAll=true",
    "cas.authn.x509.crlFetcher=ldap",
    "cas.authn.x509.ldap.ldapUrl=ldap://localhost:1389",
    "cas.authn.x509.ldap.useSsl=false",
    "cas.authn.x509.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.x509.ldap.searchFilter=cn=X509",
    "cas.authn.x509.ldap.bindDn=${ldap.managerDn}",
    "cas.authn.x509.ldap.bindCredential=${ldap.managerPassword}"
})
@EnableScheduling
public class LdaptiveResourceCRLFetcherTests extends AbstractX509LdapTests implements InitializingBean {
    private static final int LDAP_PORT = 1389;

    @Autowired
    @Qualifier("crlFetcher")
    private CRLFetcher fetcher;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeAll
    public static void bootstrapTests() throws Exception {
        initDirectoryServer(LDAP_PORT);
        AbstractX509LdapTests.bootstrap(LDAP_PORT);
    }

    @Override
    public void afterPropertiesSet() {
        SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
    }

    @Test
    public void getCrlFromLdap() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        val cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);

        for (var i = 0; i < 10; i++) {
            val checker =
                new CRLDistributionPointRevocationChecker(false, new AllowRevocationPolicy(), null,
                    cache, fetcher, true);
            val cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }
    }

    @Test
    public void getCrlFromLdapWithNoCaching() throws Exception {
        for (var i = 0; i < 10; i++) {
            CacheManager.getInstance().removeAllCaches();
            val cache = new Cache("crlCache-1", 100, false, false, 20, 10);
            CacheManager.getInstance().addCache(cache);
            val checker = new CRLDistributionPointRevocationChecker(
                false, new AllowRevocationPolicy(), null,
                cache, fetcher, true);
            val cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }
    }
}
