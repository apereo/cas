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
 * Unit test for {@link RegisteredServiceImpl}.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 *
 */
@RunWith(Parameterized.class)
public class RegisteredServiceImplTests {

    private final RegisteredServiceImpl service;

    private final String serviceToMatch;

    private final boolean expected;

    public RegisteredServiceImplTests(
            final RegisteredServiceImpl service,
            final String serviceToMatch,
            final boolean expectedResult) {
        this.service = service;
        this.serviceToMatch = serviceToMatch;
        this.expected = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                // Allow all paths on single host
                {
                        newService("https://host.vt.edu/**"),
                        "https://host.vt.edu/a/b/c?a=1&b=2",
                        true,
                },
                // Global catch-all for HTTP
                {
                        newService("http://**"),
                        "http://host.subdomain.example.com/service",
                        true,
                },
                // Null case
                {
                        newService("https:/example.com/**"),
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


    private static RegisteredServiceImpl newService(final String id) {
        final RegisteredServiceImpl service = new RegisteredServiceImpl();
        service.setServiceId(id);
        return service;
    }
}
