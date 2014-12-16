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
package org.jasig.cas.web.view;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.web.AbstractServiceValidateControllerTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;

import static org.junit.Assert.*;


/**
 * Unit tests for {@link org.jasig.cas.web.view.Cas20ResponseView}.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class Cas30ResponseViewTests extends AbstractServiceValidateControllerTests {

    @Autowired
    @Qualifier("protocolCas3ViewResolver")
    private ViewResolver resolver;

    @Test
    public void verifyView() throws Exception {
        final ModelAndView modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl();
        final JstlView v = (JstlView) resolver.resolveViewName(modelAndView.getViewName(), Locale.getDefault());
        final MockHttpServletRequest req = new MockHttpServletRequest(new MockServletContext());
        v.setServletContext(req.getServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                new GenericWebApplicationContext(req.getServletContext()));

        final Cas30ResponseView view = new Cas30ResponseView(v);
        final MockHttpServletResponse resp = new MockHttpServletResponse();
        view.render(modelAndView.getModel(), req, resp);

        assertNotNull(req.getAttribute(Cas30ResponseView.MODEL_ATTRIBUTE_NAME_ATTRIBUTES));
        assertNotNull(req.getAttribute(Cas30ResponseView.MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertNotNull(req.getAttribute(Cas30ResponseView.MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertNotNull(req.getAttribute(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
    }

}
