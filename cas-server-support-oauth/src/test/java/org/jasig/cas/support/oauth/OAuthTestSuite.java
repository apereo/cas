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
package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.web.OAuth20AccessTokenControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20CallbackAuthorizeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20WrapperControllerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({OAuth20AccessTokenControllerTests.class, OAuth20AuthorizeControllerTests.class,
                     OAuth20CallbackAuthorizeControllerTests.class, OAuth20ProfileControllerTests.class,
                     OAuth20WrapperControllerTests.class})
/**
 * OAuth test suite that runs all test in a batch.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuthTestSuite {}
