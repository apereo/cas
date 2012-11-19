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

import org.jasig.cas.adaptors.ldap.lppe.web.flow.LdapPasswordPolicyAwareAuthenticationViaFormAction;

/**
 * Using the <code>configuration</configuration>, defines an abstract mechanism by which
 * the password policy may be examined for instance for expiration warnings, strength, etc.
 * The examiner may only be invoked after authentication has taken place and is dependent on the
 * authentication handler {@link LdapPasswordPolicyAwareAuthenticationHandler} which is responsible for
 * constructing and retrieving the configuration from ldap. 
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public interface LdapPasswordPolicyExaminer {
    /**
     * Examine the underlying password policy based on the <code>configuration</code> provided.
     * 
     * @param configuration The password policy configuration assembled by the authentication handler.
     * 
     * @throws LdapPasswordPolicyAuthenticationException
     * @see {@link LdapPasswordPolicyAwareAuthenticationViaFormAction}
     * @see {@link LdapPasswordPolicyAwareAuthenticationHandler}
     */
    void examinePasswordPolicy(final LdapPasswordPolicyConfiguration configuration) throws LdapPasswordPolicyAuthenticationException;
}
