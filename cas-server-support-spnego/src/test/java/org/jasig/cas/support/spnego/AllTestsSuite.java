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
package org.jasig.cas.support.spnego;


import org.jasig.cas.support.spnego.authentication.handler.support.JCSIFSpnegoAuthenticationHandlerTests;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentialsTests;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentialsToPrincipalResolverTests;
import org.jasig.cas.support.spnego.web.flow.client.AllSpnegoKnownClientSystemsFilterActionTest;
import org.jasig.cas.support.spnego.web.flow.client.LdapSpnegoKnownClientSystemsFilterActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JCSIFSpnegoAuthenticationHandlerTests.class,
        SpnegoCredentialsTests.class,
        SpnegoCredentialsToPrincipalResolverTests.class,
        AllSpnegoKnownClientSystemsFilterActionTest.class,
        LdapSpnegoKnownClientSystemsFilterActionTests.class
})
public class AllTestsSuite {
}
