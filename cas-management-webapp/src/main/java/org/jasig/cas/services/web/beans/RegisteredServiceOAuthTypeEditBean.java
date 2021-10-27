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

package org.jasig.cas.services.web.beans;


import java.io.Serializable;

/**
 * Defines service type for OAuth, etc.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceOAuthTypeEditBean implements Serializable {
    private static final long serialVersionUID = -3619380614276733103L;

    private String clientSecret;
    private String clientId;
    private boolean bypass;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(final boolean bypass) {
        this.bypass = bypass;
    }
}
