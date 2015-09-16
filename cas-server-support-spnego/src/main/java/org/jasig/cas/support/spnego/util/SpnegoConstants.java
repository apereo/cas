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
package org.jasig.cas.support.spnego.util;

/**
 * Spnego Constants.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
public interface SpnegoConstants {

    /** The header authenticate. */
    String HEADER_AUTHENTICATE = "WWW-Authenticate";

    /** The header authorization. */
    String HEADER_AUTHORIZATION = "Authorization";

    /** The header user agent. */
    String HEADER_USER_AGENT = "User-Agent";

    /** The negotiate. */
    String NEGOTIATE = "Negotiate";

    /** The spnego first time. */
    String SPNEGO_FIRST_TIME = "spnegoFirstTime";

    /** The spnego credentials. */
    String SPNEGO_CREDENTIALS = "spnegoCredentials";

    /** The ntlm. */
    String NTLM = "NTLM";
}
