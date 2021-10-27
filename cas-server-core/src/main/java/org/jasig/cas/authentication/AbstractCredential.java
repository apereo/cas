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

/**
 * Base class for CAS credentials that are safe for long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public abstract class AbstractCredential implements Credential, CredentialMetaData, Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = 8196868021183513898L;

    /**
     * @return The credential identifier.
     */
    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof  Credential && getClass().equals(other.getClass())) {
            return getId().equals(((Credential) other).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(11, 41);
        builder.append(getClass().getName());
        builder.append(getId());
        return builder.toHashCode();
    }
}
