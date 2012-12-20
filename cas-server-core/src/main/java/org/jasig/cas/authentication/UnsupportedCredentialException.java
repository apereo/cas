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

import javax.security.auth.login.CredentialException;

/**
 * Indicates an error condition where no authentication handler exists that supports a credential, i.e.
 * {@link AuthenticationHandler#supports(Credential)} is false for the
 * credential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 */
public final class UnsupportedCredentialException extends CredentialException {
    /** Unique ID for serializing. */
    private static final long serialVersionUID = 4995877291235121527L;

    private final Credential credential;

    public UnsupportedCredentialException(final Credential credential) {
        this.credential = credential;
    }
}
