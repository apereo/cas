/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.authentication.service;

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.PrincipalResolver;
import org.junit.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsToPrincipalResolverTests extends
    TestCase {

    private final PrincipalResolver resolver = new HttpBasedServicePrincipalResolver();

    public void testInValidSupportsCredentials() {
        Assert.assertFalse(this.resolver.supports(TestUtils
                .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testNullSupportsCredentials() {
        Assert.assertFalse(this.resolver.supports(null));
    }

    public void testValidSupportsCredentials() {
        Assert.assertTrue(this.resolver.supports(TestUtils
                .getHttpBasedServiceCredentials()));
    }

    public void testValidCredentials() {
        assertEquals(this.resolver.resolve(
                TestUtils.getHttpBasedServiceCredentials()).getId(), TestUtils
            .getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }
}