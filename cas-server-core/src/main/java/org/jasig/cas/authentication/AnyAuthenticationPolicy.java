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
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AnyAuthenticationPolicy implements AuthenticationPolicy {

    /** Flag to try all credentials before policy is satisfied. */
    private boolean tryAll = false;

    /**
     * Sets the flag to try all credentials before the policy is satisfied.
     * This flag is disabled by default such that the policy is satisfied immediately upon the first
     * successfully authenticated credential.
     *
     * @param tryAll True to force all credentials to be authenticated, false otherwise.
     */
    public void setTryAll(final boolean tryAll) {
        this.tryAll = tryAll;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        if (this.tryAll) {
            return authn.getCredentials().size() == authn.getSuccesses().size() + authn.getFailures().size();
        }
        return authn.getSuccesses().size() > 0;
    }
}
