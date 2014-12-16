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

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetaData}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class BasicCredentialMetaData implements CredentialMetaData, Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = 4929579849241505377L;

    /** Credential type unique identifier. */
    private final String id;

    /** Type of original credential. */
    private Class<? extends Credential> credentialClass;

    /** No-arg constructor for serialization support. */
    private BasicCredentialMetaData() {
        this.id = null;
    }

    /**
     * Creates a new instance from the given credential.
     *
     * @param credential Credential for which metadata should be created.
     */
    public BasicCredentialMetaData(final Credential credential) {
        this.id = credential.getId();
        this.credentialClass = credential.getClass();
    }

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the type of the original credential.
     *
     * @return Non-null credential class.
     */
    public Class<? extends Credential> getCredentialClass() {
        return this.credentialClass;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 21).append(this.id).append(this.credentialClass).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BasicCredentialMetaData)) {
            return false;
        }
        final BasicCredentialMetaData md = (BasicCredentialMetaData) other;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.id, md.id);
        builder.append(this.credentialClass, md.credentialClass);
        return builder.isEquals();
    }
}
