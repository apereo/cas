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
package org.jasig.cas.adaptors.ldap.lppe.ad;

import org.ldaptive.auth.AuthenticationResponse;

/**
 * An extension of ldaptive's ActiveDirectoryAuthenticationResponseHandler that allows the plugging in
 * a custom implementation of the ActiveDirectoryAccountState object.
 * @author Misagh Moayyed
 * @since 4.0
 * @see ActiveDirectoryAccountState
 */
public class ActiveDirectoryAuthenticationResponseHandler extends org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler {

    @Override
    public void handle(final AuthenticationResponse response) {
        if (response.getMessage() != null) {
            final org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error adError =
                    org.ldaptive.auth.ext.ActiveDirectoryAccountState.Error.parse(response.getMessage());
            if (adError != null) {
                response.setAccountState(new ActiveDirectoryAccountState(adError));
            }
        }
    }
}
