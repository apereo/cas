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
package org.jasig.cas.authentication.support;

import java.util.List;
import javax.security.auth.login.LoginException;

import org.jasig.cas.MessageDescriptor;
import org.ldaptive.auth.AuthenticationResponse;

/**
 * Strategy pattern for handling directory-specific account state data.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface AccountStateHandler {
    /**
     * Handles the account state producing an error or warning messages as appropriate to the state.
     *
     * @param response LDAP authentication response containing attributes, response controls, and account state that
     *                 can be used to determine user account state.
     * @param configuration Password policy configuration.
     *
     * @return  List of warning messages.
     *
     * @throws LoginException When account state causes authentication failure.
     */
    List<MessageDescriptor> handle(AuthenticationResponse response, LdapPasswordPolicyConfiguration configuration)
            throws LoginException;
}
