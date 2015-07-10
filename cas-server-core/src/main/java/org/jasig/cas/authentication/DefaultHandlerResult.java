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
package org.jasig.cas.authentication;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Contains information about a successful authentication produced by an {@link AuthenticationHandler}.
 * Handler results are naturally immutable since they contain sensitive information that should not be modified outside
 * the {@link AuthenticationHandler} that produced it.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class DefaultHandlerResult implements HandlerResult {

    /** Serialization support. */
    private static final long serialVersionUID = -3113998493287982485L;

    /** The name of the authentication handler that successfully authenticated a credential. */
    private String handlerName;

    /** Credential meta data. */
    private CredentialMetaData credentialMetaData;

    /** Resolved principal for authenticated credential. */
    private Principal principal;

    /** List of warnings issued by the authentication source while authenticating the credential. */
    private List<MessageDescriptor> warnings;

    /** No-arg constructor for serialization support. */
    private DefaultHandlerResult() {}

    /**
     * Instantiates a new handler result.
     *
     * @param source the source
     * @param metaData the meta data
     */
    public DefaultHandlerResult(final AuthenticationHandler source, final CredentialMetaData metaData) {
        this(source, metaData, null, null);
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source the source
     * @param metaData the meta data
     * @param p the p
     */
    public DefaultHandlerResult(final AuthenticationHandler source, final CredentialMetaData metaData, final Principal p) {
        this(source, metaData, p, null);
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source the source
     * @param metaData the meta data
     * @param warnings the warnings
     */
    public DefaultHandlerResult(
            final AuthenticationHandler source, final CredentialMetaData metaData, final List<MessageDescriptor> warnings) {
        this(source, metaData, null, warnings);
    }

    /**
     * Instantiates a new handler result.
     *
     * @param source the source
     * @param metaData the meta data
     * @param p the p
     * @param warnings the warnings
     */
    public DefaultHandlerResult(
            final AuthenticationHandler source,
            final CredentialMetaData metaData,
            final Principal p,
            final List<MessageDescriptor> warnings) {
        Assert.notNull(source, "Source cannot be null.");
        Assert.notNull(metaData, "Credential metadata cannot be null.");
        this.handlerName = source.getName();
        if (!StringUtils.hasText(this.handlerName)) {
            this.handlerName = source.getClass().getSimpleName();
        }
        this.credentialMetaData = metaData;
        this.principal = p;
        this.warnings = warnings;
    }

    @Override
    public String getHandlerName() {
        return this.handlerName;
    }

    @Override
    public CredentialMetaData getCredentialMetaData() {
        return this.credentialMetaData;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public List<MessageDescriptor> getWarnings() {
        return this.warnings == null
                ? Collections.<MessageDescriptor>emptyList()
                : Collections.unmodifiableList(this.warnings);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(109, 31);
        builder.append(this.handlerName);
        builder.append(this.credentialMetaData);
        builder.append(this.principal);
        builder.append(this.warnings);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DefaultHandlerResult)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final DefaultHandlerResult other = (DefaultHandlerResult) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.handlerName, other.handlerName);
        builder.append(this.credentialMetaData, other.credentialMetaData);
        builder.append(this.principal, other.principal);
        builder.append(this.warnings, other.warnings);
        return builder.isEquals();
    }

    @Override
    public String toString() {
        return this.handlerName + ':' + this.credentialMetaData;
    }
}
