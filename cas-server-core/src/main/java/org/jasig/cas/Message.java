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
package org.jasig.cas;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class Message {

    private String code;

    private String defaultMessage;

    private Object[] params;

    public Message(final String code, final String defaultMessage, final Object ... params)
    {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public Object[] getParams() {
        return params;
    }
}
