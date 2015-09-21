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

package org.jasig.cas.support.saml;

import org.jasig.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulatorTests;
import org.jasig.cas.support.saml.authentication.SamlAuthenticationRequestTests;
import org.jasig.cas.support.saml.authentication.principal.GoogleAccountsServiceTests;
import org.jasig.cas.support.saml.authentication.principal.SamlServiceTests;
import org.jasig.cas.support.saml.util.SamlCompliantUniqueTicketIdGeneratorTests;
import org.jasig.cas.support.saml.web.flow.mdui.SamlMetadataUIParserActionTests;
import org.jasig.cas.support.saml.web.support.GoogleAccountsArgumentExtractorTests;
import org.jasig.cas.support.saml.web.support.SamlArgumentExtractorTests;
import org.jasig.cas.support.saml.web.support.WebUtilTests;
import org.jasig.cas.support.saml.web.view.Saml10FailureResponseViewTests;
import org.jasig.cas.support.saml.web.view.Saml10SuccessResponseViewTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all SAML tests.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        GoogleAccountsServiceTests.class,
        SamlServiceTests.class,
        SamlAuthenticationMetaDataPopulatorTests.class,
        SamlAuthenticationRequestTests.class,
        SamlCompliantUniqueTicketIdGeneratorTests.class,
        SamlMetadataUIParserActionTests.class,
        GoogleAccountsArgumentExtractorTests.class,
        SamlArgumentExtractorTests.class,
        WebUtilTests.class,
        Saml10FailureResponseViewTests.class,
        Saml10SuccessResponseViewTests.class
})
public final class AllTestsSuite {
}
