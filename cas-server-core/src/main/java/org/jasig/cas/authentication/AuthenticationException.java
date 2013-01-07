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

import java.util.Collections;
import java.util.Map;

/**
 * Describes an authentication failure reported by an {@link AuthenticationManager}. Since an authentication manager
 * may process several credentials, it records the result for each credential, including both successes and failures.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1510455317102913662L;

    private Map<Credential, HandlerResult> successes;

    private Map<Credential, HandlerError> failures;

    /**
     * Creates a new instance with information about successful and failed credentials.
     *
     * @param successes Map of credential to successful authentication handler results. There should be one entry
     *                  for every successfully validated credential.
     * @param failures Map of credential to failed authentication handler results. There should be one entry for
     *                 every unsuccessfully validated credential.
     */
    public AuthenticationException(
            final Map<Credential, HandlerResult> successes,
            final Map<Credential, HandlerError> failures) {

        this.successes = successes;
        this.failures = failures;
    }

    /**
     * Gets an immutable map of successful authentication handler results.
     *
     * @return Immutable map of information about successful credentials.
     */
    public Map<Credential, HandlerResult> getSuccesses() {
        return Collections.unmodifiableMap(this.successes);
    }

    /**
     * Gets an immutable map of failed authentication handler results.
     *
     * @return Immutable map of information about failed credentials.
     */
    public Map<Credential, HandlerError> getFailures() {
        return Collections.unmodifiableMap(this.failures);
    }
}
