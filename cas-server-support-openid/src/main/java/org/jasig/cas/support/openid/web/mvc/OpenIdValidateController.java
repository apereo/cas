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

package org.jasig.cas.support.openid.web.mvc;

import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.web.AbstractServiceValidateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An Openid controller that delegates to its own views on service validates.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("openIdValidateController")
public class OpenIdValidateController extends AbstractServiceValidateController {

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    @Autowired
    public void setValidationSpecificationClass(@Value("org.jasig.cas.validation.Cas20WithoutProxyingValidationSpecification")
                                                final Class<?> validationSpecificationClass) {
        super.setValidationSpecificationClass(validationSpecificationClass);
    }

    @Override
    @Autowired
    public void setFailureView(@Value("casOpenIdServiceFailureView") final String failureView) {
        super.setFailureView(failureView);
    }

    @Override
    @Autowired
    public void setSuccessView(@Value("casOpenIdServiceSuccessView") final String successView) {
        super.setSuccessView(successView);
    }

    @Override
    @Autowired
    public void setProxyHandler(@Qualifier("proxy20Handler") final ProxyHandler proxyHandler) {
        super.setProxyHandler(proxyHandler);
    }
}
