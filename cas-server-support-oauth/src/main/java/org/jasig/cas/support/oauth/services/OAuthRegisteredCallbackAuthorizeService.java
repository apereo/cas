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
package org.jasig.cas.support.oauth.services;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.support.oauth.OAuthConstants;

/**
 * OAuth registered service that denotes the callback authorized url.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public final class OAuthRegisteredCallbackAuthorizeService extends RegexRegisteredService {

    private static final long serialVersionUID = 2993846310010319047L;

    /**
     * Sets the callback authorize url.
     *
     * @param url the new callback authorize url
     */
    public void setCallbackAuthorizeUrl(final String url) {
        if (!url.endsWith(OAuthConstants.CALLBACK_AUTHORIZE_URL)) {
            throw new IllegalArgumentException("Calllback authorize url must end with "
                                                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        }
        super.setServiceId(url);
    }

    @Override
    public void setServiceId(final String id) {
        this.setCallbackAuthorizeUrl(id);
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new OAuthRegisteredCallbackAuthorizeService();
    }
}
