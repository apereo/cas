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
 * Describes a credential provided for authentication. Implementations should expect instances of this type to be
 * stored for periods of time equal to the length of the SSO session or longer, which necessitates consideration of
 * serialization and security. All implementations MUST be serializable and secure with respect to long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface CredentialMetaData {
    /**
     * Gets a unique identifier for the kind of credential this represents.
     *
     * @return Unique identifier for the given type of credential.
     */
    String getId();
}
