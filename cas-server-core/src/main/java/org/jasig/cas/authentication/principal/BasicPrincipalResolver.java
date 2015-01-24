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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;

import javax.validation.constraints.NotNull;

/**
 * Provides the most basic means of principal resolution by mapping
 * {@link org.jasig.cas.authentication.Credential#getId()} onto
 * {@link org.jasig.cas.authentication.principal.Principal#getId()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class BasicPrincipalResolver implements PrincipalResolver {

    /** Factory to create the principal type. **/
    @NotNull
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @Override
    public Principal resolve(final Credential credential) {
        return this.principalFactory.createPrincipal(credential.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getId() != null;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
