/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
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
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class RegexRegisteredServiceTests {
    
    private RegexRegisteredService service;
    
    private String serviceToMatch;

    private boolean expected;
    

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


    public RegexRegisteredServiceTests(
            final RegexRegisteredService service,
            final String serviceToMatch,
            final boolean expectedResult) {
        this.service = service;
        this.serviceToMatch = serviceToMatch;
        this.expected = expectedResult;
    }


    @Test
    public void testMatches() throws Exception {
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
