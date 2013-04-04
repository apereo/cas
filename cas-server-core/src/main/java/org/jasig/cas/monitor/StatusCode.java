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
package org.jasig.cas.monitor;

/**
 * Monitor status code inspired by HTTP status codes.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public enum StatusCode {
    ERROR(500),
    WARN(400),
    INFO(300),
    OK(200),
    UNKNOWN(100);

    /** Status code numerical value. */
    private final int value;


    /**
     * Creates a new instance with the given numeric value.
     *
     * @param numericValue Numeric status code value.
     */
    StatusCode(final int numericValue) {
        this.value = numericValue;
    }


    /**
     * Gets the numeric value of the status code.  Higher values describe more severe conditions.
     *
     * @return Numeric status code value.
     */
    public int value() {
        return this.value;
    }
}
