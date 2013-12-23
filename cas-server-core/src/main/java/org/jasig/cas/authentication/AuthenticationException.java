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
 * Authentication raised by {@link AuthenticationManager} to signal authentication failure.
 * Authentication failure typically occurs when one or more {@link AuthenticationHandler} components
 * fail to authenticate credentials. This exception contains information about handler successes
 * and failures that may be used by higher-level components to determine subsequent behavior.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationException extends Exception {

    /** Serialization metadata. */
    private static final long serialVersionUID = -6032827784134751797L;

    /** Immutable map of handler names to the errors they raised. */
    private final Map<String, Class<? extends Exception>> handlerErrors;

    /** Immutable map of handler names to an authentication success metadata instance. */
    private final Map<String, HandlerResult> handlerSuccesses;

    /**
     * Creates a new instance for the case when no handlers were attempted, i.e. no successes or failures.
     */
    public AuthenticationException() {
        this(
            "No supported authentication handlers found for given credentials.",
            Collections.<String, Class<? extends Exception>>emptyMap(),
            Collections.<String, HandlerResult>emptyMap());
    }

    /**
     * Creates a new instance for the case when no handlers succeeded.
     *
     * @param handlerErrors Map of handler names to errors.
     */
    public AuthenticationException(final Map<String, Class<? extends Exception>> handlerErrors) {
        this(handlerErrors, Collections.<String, HandlerResult>emptyMap());
    }

    /**
     * Creates a new instance for the case when there are both handler successes and failures.
     *
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public AuthenticationException(
            final Map<String, Class<? extends Exception>> handlerErrors, final Map<String, HandlerResult> handlerSuccesses) {
        this(
            String.format("%s errors, %s successes", handlerErrors.size(), handlerSuccesses.size()),
            handlerErrors,
            handlerSuccesses);
    }

    /**
     * Creates a new instance for the case when there are both handler successes and failures and a custom
     * error message is required.
     *
     * @param handlerErrors Map of handler names to errors.
     * @param handlerSuccesses Map of handler names to authentication successes.
     */
    public AuthenticationException(
            final String message,
            final Map<String, Class<? extends Exception>> handlerErrors,
            final Map<String, HandlerResult> handlerSuccesses) {
        super(message);
        this.handlerErrors = Collections.unmodifiableMap(handlerErrors);
        this.handlerSuccesses = Collections.unmodifiableMap(handlerSuccesses);
    }

    /**
     * Gets an unmodifable map of handler names to errors.
     *
     * @return Immutable map of handler names to errors.
     */
    public Map<String, Class<? extends Exception>> getHandlerErrors() {
        return this.handlerErrors;
    }

    /**
     * Gets an unmodifable map of handler names to authentication successes.
     *
     * @return Immutable map of handler names to authentication successes.
     */
    public Map<String, HandlerResult> getHandlerSuccesses() {
        return this.handlerSuccesses;
    }
}
