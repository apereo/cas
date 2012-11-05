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
package org.jasig.cas.adaptors.ldap.lppe;

import org.jasig.cas.adaptors.ldap.LdapAuthenticationException;

/**
 * Indicates an ldap authentication exception. Specific authentication error codes may be explained
 * by indicating the <code>type</code>.
 * 
 * @see #getType()
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public abstract class LdapPasswordPolicyAuthenticationException extends LdapAuthenticationException {

    public static final String CODE_PASSWORD_CHANGE  = "screen.accounterror.lppe.message";

    private static final long  serialVersionUID      = 4365292208441435202L;

    public LdapPasswordPolicyAuthenticationException(final String msg, final String type) {
        super(msg, type);
    }
}
