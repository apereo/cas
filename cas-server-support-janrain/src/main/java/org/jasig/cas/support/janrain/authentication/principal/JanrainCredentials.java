/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jasig.cas.support.janrain.authentication.principal;

import java.util.Map;
import java.util.HashMap;

import org.springframework.util.Assert;

import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.NotNull;
import org.jasig.cas.authentication.principal.Credentials;
import com.googlecode.janrain4j.api.engage.response.profile.Profile;

/**
 * This class converts a Janrain Engage profile into a CAS-compatible credential.
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class JanrainCredentials implements Credentials {
    
    private static final long serialVersionUID = 2749515040385101768L;

    /** The token that will sent to the Janrain user_info service */
    private String token;
    
    private String identifier;
    
    private Map<String, Object> userAttributes;

    private Profile userProfile;

    public JanrainCredentials(final String token) {
        Assert.notNull(token, "token cannot be null");
        this.token = token;
    }

    public final void setToken(final String token) {
        this.token = token;
    }

    public final String getToken() {
        return this.token;
    }
    
    public final void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public final String getIdentifier() {
        return this.identifier;
    }

    public final void setUserProfile(Profile userProfile) {
        this.userProfile = userProfile;
    }

    public final Profile getUserProfile() {
        return this.userProfile;
    }
    
    public void setUserAttributes(Profile userProfile) {
     
        Map<String,Object> userAttributes = new HashMap<String,Object>();

        if(userProfile.getProviderName() != null){
            userAttributes.put("ProviderName", userProfile.getProviderName());
        }
        if(userProfile.getPrimaryKey() != null){
            userAttributes.put("PrimaryKey", userProfile.getPrimaryKey());
        }
        if(userProfile.getDisplayName() != null){
            userAttributes.put("DisplayName", userProfile.getDisplayName());
        }
        if(userProfile.getName().getFamilyName() != null){
            userAttributes.put("FamilyName", userProfile.getName().getFamilyName());
        }
        if(userProfile.getName().getGivenName() != null){
            userAttributes.put("GivenName", userProfile.getName().getGivenName());
        }
        if(userProfile.getBirthday() != null){
            userAttributes.put("Birthday", userProfile.getBirthday());
        }
        if(userProfile.getVerifiedEmail() != null){
            userAttributes.put("Email", userProfile.getVerifiedEmail());
        }
        if(userProfile.getProviderName() != null){
            userAttributes.put("PhoneNumber", userProfile.getPhoneNumber());
        }
        if(userProfile.getPhoto() != null){
            userAttributes.put("PhotoURL", userProfile.getPhoto());
        }
        if(userProfile.getUrl() != null){
            userAttributes.put("Url", userProfile.getUrl());
        }
        if(userProfile.getUtcOffset() != null){
            userAttributes.put("UTCoffset", userProfile.getUtcOffset());
        }
        if(userProfile.getGender() != null){
            userAttributes.put("Gender", userProfile.getGender());
        }
        this.userAttributes = userAttributes;
    }

    public final Map<String, Object> getUserAttributes() {
        return this.userAttributes;
    }

    public String toString() {
        if (StringUtils.isNotBlank(this.identifier)){
            return this.identifier;
        } else {
            return "[janrain token: " + this.token + "]";
        }
    }
}
