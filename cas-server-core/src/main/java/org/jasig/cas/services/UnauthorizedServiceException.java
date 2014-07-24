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
package org.jasig.cas.services;

/**
 * Exception that is thrown when an Unauthorized Service attempts to use CAS.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public class UnauthorizedServiceException extends RuntimeException {

    /** The Unique ID for serialization. */
    private static final long serialVersionUID = 3905807495715960369L;
    
    /** Error code that indicates the service is unauthorized for use. **/
    public static final String CODE_UNAUTHZ_SERVICE = "screen.service.error.message";
    
    /** Exception object that indicates the service manager is empty with no service definitions. **/
    public static final String CODE_EMPTY_SVC_MGMR = "screen.service.empty.error.message";
        
    private String code = null;
    
    /**
     * Construct the exception object with the associated error code.
     * @param message the error message
     */
    public UnauthorizedServiceException(final String message) {
        super(message);
    }
    
    /**
     * Constructs an UnauthorizedServiceException with a custom message and the
     * root cause of this exception.
     *
     * @param message an explanatory message. Maybe null or blank.
     * @param code the error code mapped to the messaged bundle.
     */
    public UnauthorizedServiceException(final String code, final String message) {
        this(message);
        this.code = code;
    }
    /**
     * Constructs an UnauthorizedServiceException with a custom message and the
     * root cause of this exception.
     *
     * @param message an explanatory message.
     * @param cause the root cause of the exception.
     */
    public UnauthorizedServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    /**
     * The error code associated with this exception.
     * @return the error code.
     */
    public final String getCode() {
        return this.code;
    }
}
