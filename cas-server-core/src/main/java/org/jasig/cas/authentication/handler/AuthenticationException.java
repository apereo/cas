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
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.RootCasException;

import javax.validation.constraints.NotNull;

/**
 * The most generic type of authentication exception that one can catch if not
 * sure what specific implementation will be thrown. Top of the tree of all
 * other AuthenticationExceptions.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 * @deprecated As of 4.1, the class is required to note its abstractness in the name and will be renamed in the future.
 */
@Deprecated
public abstract class AuthenticationException extends RootCasException {

    /** Serializable ID. */
    private static final long serialVersionUID = 3906648604830611762L;

    private static final String DEFAULT_TYPE = "error";

     /** The error type that provides additional info about the nature of the exception cause. **/
    private final String type;

    /**
     * Instantiates a new authentication exception.
     *
     * @param code the code
     */
    public AuthenticationException(final String code) {
        this(code, "", DEFAULT_TYPE);
    }

    /**
     * Instantiates a new authentication exception.
     *
     * @param code the code
     * @param msg the msg
     */
    public AuthenticationException(final String code, final String msg) {
        this(code, msg, DEFAULT_TYPE);
    }

    /**
     * @param type The type of the error message that caused the exception to be thrown. By default,
     * all errors are considered of <code>error</code>.
     * @param code the exception code
     * @param msg the error message
     */
    public AuthenticationException(final String code, final String msg, @NotNull final String type) {
        super(code, msg);
        this.type = type;
    }

    /**
     * @param code the exception code
     * @param throwable the exception that originally caused the authentication failure
     */
    public AuthenticationException(final String code, final Throwable throwable) {
        super(code, throwable);
        this.type = DEFAULT_TYPE;
    }

    /**
     * Method to return the error type of this exception.
     *
     * @return the String identifier for the cause of this error.
     */
    public final String getType() {
        return this.type;
    }
}
