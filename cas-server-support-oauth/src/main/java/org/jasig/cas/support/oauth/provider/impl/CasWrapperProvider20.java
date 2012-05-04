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

import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.scribe.builder.ServiceBuilder;
import org.scribe.up.profile.JsonHelper;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.profile.UserProfileHelper;
import org.scribe.up.provider.BaseOAuth20Provider;

/**
 * This class is the OAuth provider to authenticate user in CAS server wrapping OAuth protocol.
 * 
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class CasWrapperProvider20 extends BaseOAuth20Provider {
    
    private String serverUrl;
    
    @Override
    protected void internalInit() {
        CasWrapperApi20.setServerUrl(serverUrl);
        service = new ServiceBuilder().provider(CasWrapperApi20.class).apiKey(key).apiSecret(secret)
            .callback(callbackUrl).build();
    }
    
    @Override
    protected String getProfileUrl() {
        return serverUrl + "/profile";
    }
    
    @Override
    protected UserProfile extractUserProfile(String body) {
        UserProfile userProfile = new UserProfile();
        JsonNode json = JsonHelper.getFirstNode(body);
        if (json != null) {
            UserProfileHelper.addIdentifier(userProfile, json, "id");
            json = json.get("attributes");
            if (json != null) {
                Iterator<JsonNode> nodes = json.iterator();
                while (nodes.hasNext()) {
                    json = nodes.next();
                    UserProfileHelper.addAttribute(userProfile, json, json.getFieldNames().next());
                }
            }
        }
        return userProfile;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
