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
 * Generic abstract exception to extend when you don't know what the heck is
 * going on.
 *
 * @author Scott Battaglia

 * @since 3.0
 */
public abstract class UncategorizedAuthenticationException extends
    AuthenticationException {

    /**
     * Constructor that allows for providing a custom error code for this class.
     * Error codes are often used to resolve exceptions into messages. Providing
     * a custom error code allows the use of a different message.
     *
     * @param code the custom code to use with this exception.
     */
    public UncategorizedAuthenticationException(final String code) {
        super(code);
    }

    /**
     * Constructor that allows for chaining of exceptions and a custom error
     * code.
     *
     * @param code the custom error code to use in message resolving.
     * @param throwable the chained exception.
     */
    public UncategorizedAuthenticationException(final String code,
        final Throwable throwable) {
        super(code, throwable);
    }
}
