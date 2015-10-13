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

package org.jasig.cas.support.openid;

import org.jasig.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandlerTests;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceFactoryTests;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceTests;
import org.jasig.cas.support.openid.web.flow.OpenIdSingleSignOnActionTests;
import org.jasig.cas.support.openid.web.mvc.SmartOpenIdControllerTest;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractorTests;
import org.jasig.cas.support.openid.web.support.OpenIdPostUrlHandlerMappingTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all openid test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({OpenIdPostUrlHandlerMappingTests.class, DefaultOpenIdUserNameExtractorTests.class,
        SmartOpenIdControllerTest.class, OpenIdSingleSignOnActionTests.class,
        OpenIdCredentialsAuthenticationHandlerTests.class, OpenIdServiceFactoryTests.class,
        OpenIdServiceTests.class})
public class AllTestsSuite {
}
