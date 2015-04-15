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
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.security.cert.X509Certificate;


/**
 * Test cases for {@link LdapResourceCRLFetcher}
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(JUnit4.class)
public class LdapResourceCRLFetcherTests extends AbstractLdapTests {

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
        final LdapResourceCRLFetcher fetcher = new LdapResourceCRLFetcher();
        fetcher.setObjectName("CN=x509,ou=people,dc=example,dc=org");
        final CRLDistributionPointRevocationChecker checker = new CRLDistributionPointRevocationChecker(cache, fetcher);
        checker.setThrowOnFetchFailure(true);
        checker.setUnavailableCRLPolicy(new AllowRevocationPolicy());
        final X509Certificate cert = CertUtils.readCertificate(new ClassPathResource("ldap-crl.crt"));
        checker.check(cert);


    }
}
