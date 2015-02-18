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
package org.jasig.cas.support.pac4j.test;

import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.facebook.FacebookProfile;

/**
 * Mock class for the FacebookClient.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class MockFacebookClient extends FacebookClient {

    public static final String CLIENT_NAME = "FacebookClient";

    private FacebookProfile facebookProfile;

    @Override
    public String getName() {
        return CLIENT_NAME;
    }

    @Override
    protected void internalInit() {
    }

    @Override
    protected OAuthCredentials retrieveCredentials(final WebContext context) {
        return new OAuthCredentials("fakeVerifier", getName());
    }

    @Override
    protected FacebookProfile retrieveUserProfile(final OAuthCredentials credentials, final WebContext context) {
        return facebookProfile;
    }

    public FacebookProfile getFacebookProfile() {
        return facebookProfile;
    }

    public void setFacebookProfile(final FacebookProfile facebookProfile) {
        this.facebookProfile = facebookProfile;
    }
}
