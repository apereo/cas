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
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is test cases for
 * {@link org.jasig.cas.services.DefaultRegisteredServiceAuthorizationStrategy}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAuthorizationStrategyTests {
    @Test
     public void checkDefaultAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        assertTrue(authz.isServiceAuthorized(mock(Service.class)));
        assertTrue(authz.isServiceAuthorizedForSso(mock(Service.class)));
    }

    @Test
    public void checkDisabledAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(false, true);
        assertFalse(authz.isServiceAuthorized(mock(Service.class)));
        assertTrue(authz.isServiceAuthorizedForSso(mock(Service.class)));
    }

    @Test
    public void checkDisabledSsoAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(true, false);
        assertTrue(authz.isServiceAuthorized(mock(Service.class)));
        assertFalse(authz.isServiceAuthorizedForSso(mock(Service.class)));
    }
}
