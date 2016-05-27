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
package org.jasig.cas.audit.spi;

import org.jasig.cas.authentication.Authentication;

/**
 * Strategy interface to provide principal id tokens from any given authentication event.
 *
 * Useful for authentication scenarios where there is not only one primary principal id available, but additional authentication metadata
 * in addition to custom requirement to compute and show more complex principal identifier for auditing purposes.
 * An example would be compound ids resulted from multi-legged mfa authentications, 'surrogate' authentications, etc.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.9
 */
public interface PrincipalIdProvider {

    /**
     * Return principal id from a given authentication event.
     *
     * @param authentication authentication event containing the data to computed the final principal id from
     *
     * @return computed principal id
     */
    String getPrincipalIdFrom(Authentication authentication);
}
