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

package org.jasig.cas.support.openid.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractServiceFactory;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.openid4java.message.ParameterList;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * The {@link OpenIdServiceFactory} creates {@link OpenIdService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceFactory extends AbstractServiceFactory<OpenIdService> {

    /**
     * The prefix url for OpenID (without the trailing slash).
     */
    @NotNull
    private String openIdPrefixUrl;

    public String getOpenIdPrefixUrl() {
        return openIdPrefixUrl;
    }

    public void setOpenIdPrefixUrl(final String openIdPrefixUrl) {
        this.openIdPrefixUrl = openIdPrefixUrl;
    }

    @Override
    public OpenIdService createService(final HttpServletRequest request) {
        final String service = request.getParameter(OpenIdProtocolConstants.OPENID_RETURNTO);
        final String openIdIdentity = request.getParameter(OpenIdProtocolConstants.OPENID_IDENTITY);
        final String signature = request.getParameter(OpenIdProtocolConstants.OPENID_SIG);

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
        final ParameterList paramList = new ParameterList(request.getParameterMap());

        return new OpenIdService(id, service, artifactId, openIdIdentity,
                signature, paramList, this.openIdPrefixUrl);
    }

    @Override
    public OpenIdService createService(final String id) {
        return new OpenIdService(id, id, null, null, null, null, this.openIdPrefixUrl);
    }
}
