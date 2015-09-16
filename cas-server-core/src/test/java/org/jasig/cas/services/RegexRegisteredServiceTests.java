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
package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.mock.MockService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link RegexRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@RunWith(Parameterized.class)
public class RegexRegisteredServiceTests {

    private final RegexRegisteredService service;

    private final String serviceToMatch;

    private final boolean expected;

    public RegexRegisteredServiceTests(
            final RegexRegisteredService service,
            final String serviceToMatch,
            final boolean expectedResult) {
        this.service = service;
        this.serviceToMatch = serviceToMatch;
        this.expected = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        final String domainCatchallHttp = "https*://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        final String domainCatchallHttpImap = "(https*|imaps*)://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        final String globalCatchallHttpImap = "(https*|imaps*)://.*";
        return Arrays.asList(new Object[][]{
                // CAS-1071 domain-specific HTTP catch-all #1
                {
                        newService(domainCatchallHttp),
                        "https://service.vt.edu/webapp?a=1",
                        true,
                },
                // CAS-1071 domain-specific HTTP catch-all #2
                {
                        newService(domainCatchallHttp),
                        "http://test-01.service.vt.edu/webapp?a=1",
                        true,
                },
                // CAS-1071 domain-specific HTTP catch-all #3
                {
                        newService(domainCatchallHttp),
                        "https://thepiratebay.se?service.vt.edu/webapp?a=1",
                        false,
                },
                // Domain-specific catch-all for HTTP(S)/IMAP(S) #1
                {
                        newService(domainCatchallHttpImap),
                        "http://test_service.vt.edu/login",
                        true,
                },
                // Domain-specific catch-all for HTTP(S)/IMAP(S) #2
                {
                        newService(domainCatchallHttpImap),
                        "imaps://imap-server-01.vt.edu/",
                        true,
                },
                // Global catch-all for HTTP(S)/IMAP(S) #1
                {
                        newService(globalCatchallHttpImap),
                        "https://host-01.example.com/",
                        true,
                },
                // Global catch-all for HTTP(S)/IMAP(S) #2
                {
                        newService(globalCatchallHttpImap),
                        "imap://host-02.example.edu/",
                        true,
                },
                // Null case
                {
                        newService(globalCatchallHttpImap),
                        null,
                        false,
                },
        });
    }

    @Test
    public void verifyMatches() throws Exception {
        final Service testService;
        if (serviceToMatch == null) {
            testService = null;
        } else {
            testService = new MockService(serviceToMatch);
        }
        assertEquals(expected, service.matches(testService));
    }


    private static RegexRegisteredService newService(final String id) {
        final RegexRegisteredService service = new RegexRegisteredService();
        service.setServiceId(id);
        return service;
    }
}
