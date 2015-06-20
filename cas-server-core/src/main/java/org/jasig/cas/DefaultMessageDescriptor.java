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
package org.jasig.cas;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class DefaultMessageDescriptor implements MessageDescriptor {

    /** Serialization support. */
    private static final long serialVersionUID = 1227390629186486032L;

    private final String code;

    private final String defaultMessage;

    private final Serializable[] params;

    /**
     * Instantiates a new message.
     *
     * @param code the code
     */
    public DefaultMessageDescriptor(final String code) {
        this(code, code);
    }

    /**
     * Instantiates a new message.
     *
     * @param code the code
     * @param defaultMessage the default message
     * @param params the params
     */
    public DefaultMessageDescriptor(final String code, final String defaultMessage, final Serializable... params) {
        Assert.hasText(code, "Code cannot be null or empty");
        Assert.hasText(defaultMessage, "Default message cannot be null or empty");
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.params = params;
    }

    public String getCode() {
        return this.code;
    }

    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    /**
     * Get parameters for the message.
     *
     * @return the serializable [ ]
     */
    public Serializable[] getParams() {
        if (this.params == null) {
            return null;
        }
        return ImmutableList.copyOf(this.params).toArray(new Serializable[this.params.length]);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(99, 31);
        builder.append(this.code);
        builder.append(this.defaultMessage);
        builder.append(this.params);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof DefaultMessageDescriptor)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final DefaultMessageDescriptor m = (DefaultMessageDescriptor) other;
        return this.code.equals(m.getCode())
                && this.defaultMessage.equals(m.getDefaultMessage())
                && Arrays.equals(this.params, m.getParams());
    }
}
