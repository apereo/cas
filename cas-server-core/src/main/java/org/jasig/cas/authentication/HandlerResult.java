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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.Message;
import org.jasig.cas.util.SerialUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Contains information about a successful authentication produced by an {@link AuthenticationHandler}.
 * Handler results are naturally immutable since they contain sensitive information that should not be modified outside
 * the {@link AuthenticationHandler} that produced it.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class HandlerResult implements Serializable {

    /** Serialization support. */
    private static final long serialVersionUID = 9076393135132363384L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    /** The name of the authentication handler that successfully authenticated a credential. */
    private String handlerName;

    /** Resolved principal for authenticated credential. */
    private Principal principal;

    /** List of warnings issued by the authentication source while authenticating the credential. */
    private List<Message> warnings;


    public HandlerResult(final AuthenticationHandler source) {
        this(source, null, Collections.<Message>emptyList());
    }

    public HandlerResult(final AuthenticationHandler source, final Principal p) {
        this(source, p, Collections.<Message>emptyList());
    }

    public HandlerResult(final AuthenticationHandler source, final List<Message> warnings) {
        this(source, null, Collections.<Message>emptyList());
    }

    public HandlerResult(final AuthenticationHandler source, final Principal p, final List<Message> warnings) {
        Assert.notNull(source, "Source cannot be null.");
        Assert.notNull(warnings, "Warnings cannot be null.");
        this.handlerName = source.getName();
        if (!StringUtils.hasText(this.handlerName)) {
            this.handlerName = source.getClass().getSimpleName();
        }
        this.principal = p;
        this.warnings = warnings;
    }

    public String getHandlerName() {
        return this.handlerName;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public List<Message> getWarnings() {
        return Collections.unmodifiableList(this.warnings);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(109, 31);
        builder.append(this.handlerName);
        builder.append(this.principal);
        builder.append(this.warnings);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof HandlerResult)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final HandlerResult other = (HandlerResult) obj;
        return this.handlerName.equals(other.getHandlerName()) &&
                this.principal.equals(other.getPrincipal()) &&
                this.warnings.equals(other.getWarnings());
    }


    private void writeObject(final ObjectOutputStream out) throws IOException {
        SerialUtils.writeObject(this.handlerName, out);
        SerialUtils.writeObject(this.principal, out);
        SerialUtils.writeCollection(this.warnings, out);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.handlerName = SerialUtils.readObject(String.class, in);
        this.principal = SerialUtils.readObject(Principal.class, in);
        this.warnings = SerialUtils.readList(Message.class, in);
    }
}
