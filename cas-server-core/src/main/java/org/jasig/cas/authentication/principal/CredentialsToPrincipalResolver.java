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
package org.jasig.cas.authentication.principal;

public interface CredentialsToPrincipalResolver {

    /**
     * Turn Credentials into a Principal object by analyzing the information
     * provided in the Credentials and constructing a Principal object based on
     * that information or information derived from the Credentials object.
     * 
     * @param credentials from which to resolve Principal
     * @return resolved Principal, or null if the principal could not be resolved.
     */
    Principal resolvePrincipal(Credentials credentials);

    /**
     * Determine if a credentials type is supported by this resolver. This is
     * checked before calling resolve principal.
     * 
     * @param credentials The credentials to check if we support.
     * @return true if we support these credentials, false otherwise.
     */
    boolean supports(Credentials credentials);
}
