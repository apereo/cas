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
package org.jasig.cas.support.openid;

/**
 * OpenID constants.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public final class OpenIdConstants {

    /**
     * The application callback url.
     */
    public static final String OPENID_RETURNTO = "openid.return_to";

    /**
     * The OpenID association handle.
     */
    public static final String OPENID_ASSOCHANDLE = "openid.assoc_handle";

    /**
     * The OpenID mode.
     */
    public static final String OPENID_MODE = "openid.mode";

    /**
     * The OpenID cancel mode.
     */
    public static final String CANCEL = "cancel";

    /**
     * The OpenID identity.
     */
    public static final String OPENID_IDENTITY = "openid.identity";

    /**
     * The OpenID SIG.
     */
    public static final String OPENID_SIG = "openid.sig";

    /**
     * When the user can select its own username for login.
     */
    public static final String OPENID_IDENTIFIERSELECT = "http://specs.openid.net/auth/2.0/identifier_select";

    /**
     * The name of the OpenID username for the login page.
     */
    public static final String OPENID_LOCALID = "openIdLocalId";

    /**
     * The OpenID associate mode.
     */
    public static final String ASSOCIATE = "associate";

    /**
     * Private constructor.
     */
    private OpenIdConstants() {}
}
