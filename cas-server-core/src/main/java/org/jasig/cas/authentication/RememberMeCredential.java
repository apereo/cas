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
package org.jasig.cas.authentication;

/**
 * Credential that wish to handle remember me scenarios need
 * to implement this class.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public interface RememberMeCredential extends Credential {

    /** Authentication attribute name for remember-me. **/
    String AUTHENTICATION_ATTRIBUTE_REMEMBER_ME = "org.jasig.cas.authentication.principal.REMEMBER_ME";

    /** Request parameter name. **/
    String REQUEST_PARAMETER_REMEMBER_ME = "rememberMe";

    boolean isRememberMe();

    void setRememberMe(boolean rememberMe);
}
