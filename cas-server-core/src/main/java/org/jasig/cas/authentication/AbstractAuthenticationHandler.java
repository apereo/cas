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

/**
 * Base class for all authentication handlers that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    /** Configurable handler name. */
    private String name;

    @Override
    public String getName() {
        return this.name != null ? this.name : getClass().getSimpleName();
    }

    /**
     * Sets the authentication handler name. Authentication handler names SHOULD be unique within an
     * {@link org.jasig.cas.authentication.AuthenticationManager}, and particular implementations
     * may require uniqueness. Uniqueness is a best
     * practice generally.
     *
     * @param name Handler name.
     */
    public void setName(final String name) {
        this.name = name;
    }
}
