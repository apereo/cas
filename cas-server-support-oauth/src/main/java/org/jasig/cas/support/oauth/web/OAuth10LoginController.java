/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.support.oauth.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.support.oauth.OAuthUtils;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.provider.ProvidersDefinition;
import org.scribe.up.session.HttpUserSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This class is an intermediate controller called when the user wants to delegate authentication to an OAuth provider implementing OAuth
 * protocol v1.0. At this step, the authorization url is computed and the user is redirected to it.
 *
 * @author Jerome Leleu
 * @since 3.5.1
 */
public final class OAuth10LoginController extends AbstractController {

    @NotNull
    private final ProvidersDefinition providersDefinition;

    public OAuth10LoginController(final ProvidersDefinition providersDefinition) {
        this.providersDefinition = providersDefinition;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        // get provider type
        final String providerType = request.getParameter(this.providersDefinition.getProviderTypeParameter());
        // get provider
        final OAuthProvider provider = this.providersDefinition.findProvider(providerType);

        // authorization url
        final String authorizationUrl = provider.getAuthorizationUrl(new HttpUserSession(request));

        return OAuthUtils.redirectTo(authorizationUrl);
    }
}
