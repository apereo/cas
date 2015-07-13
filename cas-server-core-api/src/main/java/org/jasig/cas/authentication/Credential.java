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
 * Describes an authentication credential. Implementations SHOULD also implement {@link CredentialMetaData} if
 * no sensitive data is contained in the credential; conversely, implementations MUST NOT implement
 * {@link CredentialMetaData} if the credential contains sensitive data (e.g. password, key material).
 *
 * @author William G. Thompson, Jr.
 * @author Marvin S. Addison
 * @see CredentialMetaData
 * @since 3.0.0
 */
public interface Credential {

    /** An ID that may be used to indicate the credential identifier is unknown. */
    String UNKNOWN_ID = "unknown";

    /**
     * Gets a credential identifier that is safe to record for logging, auditing, or presentation to the user.
     * In most cases this has a natural meaning for most credential types (e.g. username, certificate DN), while
     * for others it may be awkward to construct a meaningful identifier. In any case credentials require some means
     * of identification for a number of cases and implementers should make a best effor to satisfy that need.
     *
     * @return Non-null credential identifier. Implementers should return {@link #UNKNOWN_ID} for cases where an ID
     * is not readily available or meaningful.
     */
    String getId();
}
