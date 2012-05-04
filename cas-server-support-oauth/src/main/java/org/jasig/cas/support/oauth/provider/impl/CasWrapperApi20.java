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
package org.jasig.cas.support.oauth.provider.impl;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.utils.OAuthEncoder;

/**
 * This class represents the OAuth API implementation for the CAS OAuth wrapper.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class CasWrapperApi20 extends DefaultApi20 {
    
    private static String serverUrl = "";
    
    @Override
    public String getAccessTokenEndpoint() {
        return serverUrl + "/accessToken?";
    }
    
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(serverUrl + "/authorize?client_id=%s&redirect_uri=%s", config.getApiKey(),
                             OAuthEncoder.encode(config.getCallback()));
    }
    
    public static void setServerUrl(String url) {
        serverUrl = url;
    }
}
