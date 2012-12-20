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

import java.util.Locale;

import javax.validation.constraints.NotNull;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredential and returns a SimplePrincipal.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date: 2007-06-11 11:59:18 -0400 (Mon, 11 Jun
 * 2007) $
 * @since 3.1
 */
public final class SpnegoPrincipalResolver extends
        AbstractPersonDirectoryPrincipalResolver {
    
    public static enum Transform {NONE, UPPERCASE, LOWERCASE}
    
    @NotNull
    private Transform transformPrincipalId = Transform.NONE; 

    protected String extractPrincipalId(final Credential credential) {
        final SpnegoCredential c = (SpnegoCredential) credential;
        
        switch (this.transformPrincipalId) {
            case UPPERCASE:
                return c.getPrincipal().getId().toUpperCase(Locale.ENGLISH);
            case LOWERCASE:
                return c.getPrincipal().getId().toLowerCase(Locale.ENGLISH);
            default:
                return c.getPrincipal().getId();
        }
    }

    public boolean supports(final Credential credential) {
        return credential != null
            && SpnegoCredential.class.equals(credential.getClass());
    }
    
    public void setTransformPrincipalId(final Transform transform) {
        this.transformPrincipalId = transform;
    }
}
