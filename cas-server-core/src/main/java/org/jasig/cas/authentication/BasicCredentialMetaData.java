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

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Basic credential metadata implementation that stores credential class name as identifier.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class BasicCredentialMetaData implements CredentialMetaData, Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = -9153771561241003144L;

    /** Credential type unique identifier. */
    private final String id;

    /**
     * Creates a new instance from the given credential.
     *
     * @param credential Credential for which metadata should be created.
     */
    public BasicCredentialMetaData(final Credentials credential) {
        this.id = credential.getClass().getName();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 21).append(this.id).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof CredentialMetaData && this.id.equals(((CredentialMetaData) other).getId());
    }
}
