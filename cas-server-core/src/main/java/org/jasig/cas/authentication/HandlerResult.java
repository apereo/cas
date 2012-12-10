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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jasig.cas.Message;
import org.springframework.util.Assert;

/**
 * Contains information about a successful authentication produced by an {@link AuthenticationHandler}.
 * Handler results are naturally immutable since they contain sensitive information that should not be modified outside
 * the {@link AuthenticationHandler} that produced it.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class HandlerResult implements Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = -5862444330068435791L;

    /** The name of the authentication handler that successfully authenticated a credential. */
    private final String handlerName;

    /** Resolved principal for authenticated credential. */
    private final Principal principal;

    /** List of warnings issued by the authentication source while authenticating the credential. */
    private final List<Message> warnings;


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
        return this.warnings;
    }
}
