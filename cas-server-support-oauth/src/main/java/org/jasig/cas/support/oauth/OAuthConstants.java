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
package org.jasig.cas.support.oauth;

/**
 * This class has the main constants for the OAuth implementation.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public interface OAuthConstants {

    public static final String PROVIDER_TYPE = "providerType";

    public static final String REDIRECT_URI = "redirect_uri";

    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String CODE = "code";

    public static final String SERVICE = "service";

    public static final String THEME = "theme";

    public static final String LOCALE = "locale";

    public static final String METHOD = "method";

    public static final String TICKET = "ticket";

    public static final String STATE = "state";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String OAUTH20_CALLBACKURL = "oauth20_callbackUrl";

    public static final String OAUTH20_SERVICE_NAME = "oauth20_service_name";

    public static final String OAUTH20_STATE = "oauth20_state";

    public static final String MISSING_ACCESS_TOKEN = "missing_accessToken";

    public static final String EXPIRED_ACCESS_TOKEN = "expired_accessToken";

    public static final String CONFIRM_VIEW = "oauthConfirmView";

    public static final String ERROR_VIEW = "viewServiceErrorView";

    public static final String INVALID_REQUEST = "invalid_request";

    public static final String INVALID_GRANT = "invalid_grant";

    public static final String AUTHORIZE_URL = "authorize";

    public static final String CALLBACK_AUTHORIZE_URL = "callbackAuthorize";

    public static final String ACCESS_TOKEN_URL = "accessToken";

    public static final String PROFILE_URL = "profile";

    public static final String OAUTH10_LOGIN_URL = "oauth10login";
}
