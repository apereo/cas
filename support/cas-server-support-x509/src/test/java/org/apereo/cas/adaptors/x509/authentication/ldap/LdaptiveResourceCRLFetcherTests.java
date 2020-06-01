package org.apereo.cas.adaptors.x509.authentication.ldap;

import org.apereo.cas.adaptors.x509.BaseX509Tests;
import org.apereo.cas.adaptors.x509.authentication.CRLFetcher;
import org.apereo.cas.adaptors.x509.authentication.handler.support.AbstractX509LdapTests;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.CRLDistributionPointRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.AllowRevocationPolicy;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;


/**
 * Test cases for {@link LdaptiveResourceCRLFetcher}
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
        "cas.authn.attribute-repository.stub.attributes.certificateRevocationList=certificateRevocationList",
        "cas.authn.x509.reg-ex-trusted-issuer-dn-pattern=CN=\\\\w+,DC=jasig,DC=org",
        "cas.authn.x509.principal-type=SERIAL_NO_DN",
        "cas.authn.policy.any.try-all=true",
        "cas.authn.x509.crl-fetcher=ldap",
        "cas.authn.x509.ldap.ldap-url=ldap://localhost:1389",
        "cas.authn.x509.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.x509.ldap.search-filter=cn=X509",
        "cas.authn.x509.ldap.bind-dn=cn=Directory Manager,dc=example,dc=org",
        "cas.authn.x509.ldap.bind-credential=Password"
    })
@EnableScheduling
@Tag("Ldap")
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

    private static UserManagedCache<URI, byte[]> getCache(final int entries) {
        return UserManagedCacheBuilder.newUserManagedCacheBuilder(URI.class, byte[].class)
            .withResourcePools(ResourcePoolsBuilder.heap(entries)).build();
    }

    @Test
    public void getCrlFromLdap() throws Exception {
        val cache = getCache(100);
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
            val cache = getCache(100);
            val checker = new CRLDistributionPointRevocationChecker(
                false, new AllowRevocationPolicy(), null,
                cache, fetcher, true);
            val cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }
    }
}
