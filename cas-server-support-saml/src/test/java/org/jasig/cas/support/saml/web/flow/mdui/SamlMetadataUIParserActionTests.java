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

package org.jasig.cas.support.saml.web.flow.mdui;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link SamlMetadataUIParserActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserActionTests extends AbstractOpenSamlTests {

    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private SamlMetadataUIParserAction samlMetadataUIParserAction;

    @Autowired
    @Qualifier("samlDynamicMetadataUIParserAction")
    private SamlMetadataUIParserAction samlDynamicMetadataUIParserAction;

    @Test
    public void verifyEntityIdUIInfoExists() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlMetadataUIParserAction.ENTITY_ID_PARAMETER_NAME, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.doExecute(ctx);
        assertTrue(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }

    @Test
    public void verifyEntityIdUIInfoExistsDynamically() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlMetadataUIParserAction.ENTITY_ID_PARAMETER_NAME, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlDynamicMetadataUIParserAction.doExecute(ctx);
        assertTrue(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }

    @Test
    public void verifyEntityIdUIInfoNoParam() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("somethingelse", "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.doExecute(ctx);
        assertFalse(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }

}
