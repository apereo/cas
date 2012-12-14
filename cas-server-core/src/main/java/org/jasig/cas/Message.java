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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.util.SerialUtils;
import org.springframework.util.Assert;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class Message implements Serializable {

    /** Serialization support. */
    private static final long serialVersionUID = -9022062191460678513L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    private String code;

    private String defaultMessage;

    private Serializable[] params;


    public Message(final String code, final String defaultMessage, final Serializable... params) {
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

    public Serializable[] getParams() {
        return this.params;
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
        if (other == null || !(other instanceof Message)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final Message m = (Message) other;
        return this.code.equals(m.getCode()) &&
                this.defaultMessage.equals(m.getDefaultMessage()) &&
                Arrays.equals(this.params, m.getParams());
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        SerialUtils.writeObject(this.code, out);
        SerialUtils.writeObject(this.defaultMessage, out);
        if (this.params != null && this.params.length > 0) {
            out.writeInt(this.params.length);
            for (int i = 0; i < this.params.length; i++) {
                SerialUtils.writeObject(this.params[i], out);
            }
        } else {
            out.writeInt(0);
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.code = SerialUtils.readObject(String.class, in);
        this.defaultMessage = SerialUtils.readObject(String.class, in);
        this.params = new Serializable[in.readInt()];
        if (this.params.length > 0) {
            for (int i = 0; i < this.params.length; i++) {
                this.params[i] = SerialUtils.readObject(Serializable.class, in);
            }
        }
    }
}
