/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * Test cases for {@link LdaptiveResourceCRLFetcher}
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/x509-ldap-context.xml"})
public class LdaptiveResourceCRLFetcherTests extends AbstractX509LdapTests {


    @Autowired
    @Qualifier("ldapCertFetcher")
    private LdaptiveResourceCRLFetcher fetcher;

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
            checker.check(cert);
        }
    }

}
