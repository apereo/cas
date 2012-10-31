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

public interface LdapPasswordPolicyExaminer {
    
    /**
     * This enumeration defines a selective set of ldap user account control flags
     * that indicate various statuses of the user account. The account status
     * is a bitwise flag that may contain one of more of the following values.
     */
    public enum ActiveDirectoryUserAccountControlFlags {
        UAC_FLAG_ACCOUNT_DISABLED(2),
        UAC_FLAG_LOCKOUT(16),
        UAC_FLAG_PASSWD_NOTREQD(32),
        UAC_FLAG_DONT_EXPIRE_PASSWD(65536),
        UAC_FLAG_PASSWORD_EXPIRED(8388608);
        
        private int value;
        
        ActiveDirectoryUserAccountControlFlags(final int id) { 
            this.value = id; 
        }
        
        public final int getValue() { 
            return this.value; 
        }
    }
    
    void examinePasswordPolicy(final LdapPasswordPolicyConfiguration configuration) throws LdapPasswordPolicyAuthenticationException;
}
