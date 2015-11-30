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
package org.jasig.cas.support.spnego.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver;

import javax.validation.constraints.NotNull;
import java.util.Locale;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredential and returns a SimplePrincipal.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
public final class SpnegoPrincipalResolver extends PersonDirectoryPrincipalResolver {

    /** Tranformation types. **/
    public enum Transform {NONE, UPPERCASE, LOWERCASE}

    @NotNull
    private Transform transformPrincipalId = Transform.NONE;

    @Override
    protected String extractPrincipalId(final Credential credential) {
        final SpnegoCredential c = (SpnegoCredential) credential;
        final String id = c.getPrincipal().getId();

        switch (this.transformPrincipalId) {
        case UPPERCASE:
            return id.toUpperCase(Locale.ENGLISH);
        case LOWERCASE:
            return id.toLowerCase(Locale.ENGLISH);
        default:
            return id;
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null
                && SpnegoCredential.class.equals(credential.getClass());
    }

    public void setTransformPrincipalId(final Transform transform) {
        this.transformPrincipalId = transform;
    }
}
