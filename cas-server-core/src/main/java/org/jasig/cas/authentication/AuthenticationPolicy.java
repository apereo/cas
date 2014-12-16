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

/**
 * Stategy interface for pluggable authentication security policies.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface AuthenticationPolicy {
    /**
     * Determines whether an authentication event isSatisfiedBy arbitrary security policy.
     *
     * @param authentication Authentication event to examine for compliance with security policy.
     *
     * @return True if authentication isSatisfiedBy security policy, false otherwise.
     */
    boolean isSatisfiedBy(Authentication authentication);
}
