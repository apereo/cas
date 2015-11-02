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

package org.jasig.cas.support.saml.web.idp;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlResponseBuilder} defines the operations
 * required for building the saml response for an RP.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface SamlResponseBuilder {

    /**
     * Build response.
     *
     * @param authnRequest the authn request
     * @param request      the request
     * @param response     the response
     * @param assertion    the assertion
     * @param service      the service
     * @return the response
     * @throws Exception the exception
     */
    Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                   final HttpServletResponse response, final Assertion assertion,
                   final SamlRegisteredService service)
                    throws Exception;
}
