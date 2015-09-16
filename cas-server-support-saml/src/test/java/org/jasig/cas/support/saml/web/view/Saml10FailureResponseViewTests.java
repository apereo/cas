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
package org.jasig.cas.support.saml.web.view;

import static org.junit.Assert.*;

import java.util.Collections;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for {@link Saml10FailureResponseView} class
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 *
 */
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private final Saml10FailureResponseView view = new Saml10FailureResponseView();

    @Test
    public void verifyResponse() throws Exception {
        final MockHttpServletRequest request =  new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("TARGET", "service");

        final String description = "Validation failed";
        this.view.renderMergedOutputModel(
                Collections.<String, Object>singletonMap("description", description), request, response);

        final String responseText = response.getContentAsString();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }

}
