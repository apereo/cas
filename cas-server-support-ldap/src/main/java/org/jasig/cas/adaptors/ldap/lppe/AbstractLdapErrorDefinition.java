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
package org.jasig.cas.adaptors.ldap.lppe;

import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.adaptors.ldap.LdapAuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * An abstract implementation of the {@link LdapErrorDefinition} which expects
 * the error definition to be a regular expression pattern.
 */
public abstract class AbstractLdapErrorDefinition implements LdapErrorDefinition {

    private Pattern errorDefinition = null;
    private String  type            = null;

    public AbstractLdapErrorDefinition(final String def, final String type) {
        this.type = type;
        this.errorDefinition = Pattern.compile(def);
    }

    @Override
    public AuthenticationException getAuthenticationException(final String message) {
        return new LdapAuthenticationException(message, getType());
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getDefinition() {
        return this.errorDefinition.pattern();
    }

    @Override
    public boolean matches(final String error) {
        return this.errorDefinition.matcher(error).find();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("Definition", getDefinition())
                                        .append("Type", getType()).toString();
    }
}
