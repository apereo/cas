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
package org.jasig.cas.support.openid.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Maps requests for usernames to a page that displays the Login URL for an
 * OpenId Identity Provider.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("openIdProviderController")
public final class OpenIdProviderController extends AbstractController {

    @NotNull
    @Value("${server.prefix}/login")
    private String loginUrl;

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        return new ModelAndView("openIdProviderView", "openid_server", this.loginUrl);
    }

    public void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }
}
