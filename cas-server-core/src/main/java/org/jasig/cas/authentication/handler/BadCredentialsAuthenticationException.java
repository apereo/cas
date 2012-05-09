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
package org.jasig.cas.authentication.handler;

/**
 * Generic Bad Credentials Exception. This can be thrown when the system knows
 * the credentials are not valid specificially because they are bad. Subclasses
 * can be specific to a certain type of Credentials
 * (BadUsernamePassowrdCredentials).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BadCredentialsAuthenticationException extends
    AuthenticationException {

    /**
     * Static instance of class to prevent cost incurred by creating new
     * instance.
     */
    public static final BadCredentialsAuthenticationException ERROR = new BadCredentialsAuthenticationException();

    /** UID for serializable objects. */
    private static final long serialVersionUID = 3256719585087797044L;

    /**
     * Default constructor that does not allow the chaining of exceptions and
     * uses the default code as the error code for this exception.
     */
    public static final String CODE = "error.authentication.credentials.bad";

    /**
     * Default constructor that does not allow the chaining of exceptions and
     * uses the default code as the error code for this exception.
     */
    public BadCredentialsAuthenticationException() {
        super(CODE);
    }

    /**
     * Constructor to allow for the chaining of exceptions. Constructor defaults
     * to default code.
     * 
     * @param throwable the chainable exception.
     */
    public BadCredentialsAuthenticationException(final Throwable throwable) {
        super(CODE, throwable);
    }

    /**
     * Constructor method to allow for providing a custom code to associate with
     * this exception.
     * 
     * @param code the code to use.
     */
    public BadCredentialsAuthenticationException(final String code) {
        super(code);
    }

    /**
     * Constructor to allow for the chaining of exceptions and use of a
     * non-default code.
     * 
     * @param code the user-specified code.
     * @param throwable the chainable exception.
     */
    public BadCredentialsAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
    }
}
