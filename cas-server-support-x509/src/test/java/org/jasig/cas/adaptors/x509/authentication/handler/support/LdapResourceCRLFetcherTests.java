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

package org.jasig.cas.adaptors.x509.authentication.handler.support;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.SearchRequest;
import org.ldaptive.pool.AbstractConnectionPool;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.provider.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.cert.X509Certificate;


/**
 * Test cases for {@link LdapResourceCRLFetcher}
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/x509-ldap-context.xml"})
public class LdapResourceCRLFetcherTests extends AbstractLdapTests {

    @Autowired
    private ConnectionConfig connectionConfig;

    @Autowired
    private SearchRequest searchRequest;

    @Autowired
    private Provider provider;

    @Autowired
    private AbstractConnectionPool connectionPool;

    @BeforeClass
    public static void beforeClass() throws Exception {
        initDirectoryServer();
        getDirectory().populateEntries(new ClassPathResource("ldif/users-x509.ldif"));
    }

    @Test
    public void getCrlFromLdap() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final LdapResourceCRLFetcher fetcher = new LdapResourceCRLFetcher(this.searchRequest,
                this.connectionConfig);
        fetcher.setProvider(this.provider);

        final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
        checker.setThrowOnFetchFailure(true);
        checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
        checker.check(cert);
    }

    @Test
    public void getCrlFromLdapAndPoolConnections() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final LdapResourceCRLFetcher fetcher = new LdapResourceCRLFetcher(this.searchRequest,
                this.connectionConfig);
        fetcher.setProvider(this.provider);
        fetcher.setConnectionPool(this.connectionPool);

        final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
        checker.setThrowOnFetchFailure(true);
        checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        for (int i = 0; i < 10; i++) {
            final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
            checker.check(cert);
        }

    }

    @Test(expected = RuntimeException.class)
    public void getCrlFromLdapNoAttribute() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final LdapResourceCRLFetcher fetcher = new LdapResourceCRLFetcher(this.searchRequest,
                this.connectionConfig, "noattribute");
        fetcher.setProvider(this.provider);

        final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
        checker.setThrowOnFetchFailure(true);
        checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
        checker.check(cert);
    }

    @Test(expected = RuntimeException.class)
    public void getCrlFromLdapInvalidAttribute() throws Exception {
        CacheManager.getInstance().removeAllCaches();
        final Cache cache = new Cache("crlCache-1", 100, false, false, 20, 10);
        CacheManager.getInstance().addCache(cache);
        final LdapResourceCRLFetcher fetcher = new LdapResourceCRLFetcher(this.searchRequest,
                this.connectionConfig, "mail");
        fetcher.setProvider(this.provider);

        final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
        checker.setThrowOnFetchFailure(true);
        checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
        checker.check(cert);
    }
}
