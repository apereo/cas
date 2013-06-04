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
 * Interface for a class that fetches an account status.
 *
 * @author Jan Van der Velpen
 * @since 3.1
 */
public interface PasswordPolicyEnforcer {
    /**
     * @param userId The unique ID of the user
     * @return Number of days to the expiration date, or -1 if checks pass.
     * @throws LdapPasswordPolicyEnforcementException if the authentication fails as the result of enforcing password policy
     */
	@Deprecated
    long getNumberOfDaysToPasswordExpirationDate(String userId)
            throws LdapPasswordPolicyEnforcementException;
    /**
     * @param userId The unique ID of the user
     * @paramcredentials The credentials of the user
     * @return Number of days to the expiration date, or -1 if checks pass.
     */
    public long getNumberOfDaysToPasswordExpirationDate(final String userId, final String credentials) throws LdapPasswordPolicyEnforcementException;

}
