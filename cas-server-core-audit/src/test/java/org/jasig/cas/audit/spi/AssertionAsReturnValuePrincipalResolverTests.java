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
package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertion;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AssertionAsReturnValuePrincipalResolverTests}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.9
 */
public class AssertionAsReturnValuePrincipalResolverTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyResolverAssertionReturnValue() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationContext authnResult = TestUtils.getAuthenticationContext(getAuthenticationSystemSupport(), c);
        final Authentication authn = authnResult.getAuthentication();

        final TicketOrCredentialPrincipalResolver delegate = new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final AssertionAsReturnValuePrincipalResolver res = new AssertionAsReturnValuePrincipalResolver(delegate);
        final JoinPoint jp = mock(JoinPoint.class);
        final Assertion returnedAssertion =
                new ImmutableAssertion(authnResult.getAuthentication(), Arrays.asList(authn), authnResult.getService(), true);

        final String result = res.resolveFrom(jp, returnedAssertion);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }
}
