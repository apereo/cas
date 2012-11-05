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
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.handler.AuthenticationException;

/** 
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class LdapAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 3298900641124398115L;

    private static final String CODE = "error.ldap.authentication.credentials.bad";
    
    public LdapAuthenticationException(final Exception e) {
        super(CODE, e);
    }
    
    public LdapAuthenticationException(final Exception e, final String type) {
        super(e.getMessage(), e, CODE, type);
    }
    
    public LdapAuthenticationException(final String message, final String type) {
        super(message, CODE, type);
    }
}
