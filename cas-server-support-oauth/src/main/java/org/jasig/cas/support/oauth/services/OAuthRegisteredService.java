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
package org.jasig.cas.support.oauth.services;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegexRegisteredService;

/**
 * Some Javadoc I will add later.
 * @author Misagh Moayyed
 * @since 4.0
 */
public class OAuthRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 6784839055053605375L;

    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret = null;

    public final void setClientKey(@NotNull final String clientKey) {
        this.clientId = clientKey;
    }

    public final String getClientId() {
        return this.clientId;
    }

    public void setClientSecret(@NotNull final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public final String getClientSecret() {
        return this.clientSecret;
    }
}
