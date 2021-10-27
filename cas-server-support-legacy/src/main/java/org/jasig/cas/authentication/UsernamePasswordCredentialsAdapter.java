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
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Converts a CAS 4.0 username/password credential into a CAS 3.0 username/password credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class UsernamePasswordCredentialsAdapter implements CredentialsAdapter {
    @Override
    public Credentials convert(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            throw new IllegalArgumentException(credential + " not supported.");
        }
        final UsernamePasswordCredential original = (UsernamePasswordCredential) credential;
        final UsernamePasswordCredentials old = new UsernamePasswordCredentials();
        old.setUsername(original.getUsername());
        old.setPassword(original.getPassword());
        return old;
    }
}
