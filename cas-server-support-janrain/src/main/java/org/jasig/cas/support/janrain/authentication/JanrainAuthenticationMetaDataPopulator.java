/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jasig.cas.support.janrain.authentication;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.janrain.authentication.principal.JanrainCredentials;

/**
 * This class is a meta data populator for authentication using Janrain Engage. 
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class JanrainAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {
    
    public Authentication populateAttributes(Authentication authentication, Credentials credentials) {
        if (credentials instanceof JanrainCredentials) {
          JanrainCredentials janrainCredentials = (JanrainCredentials) credentials;
          final Principal simplePrincipal = new SimplePrincipal(authentication.getPrincipal().getId(),
                                                                  janrainCredentials.getUserAttributes());
            final MutableAuthentication mutableAuthentication = new MutableAuthentication(simplePrincipal,
                                                                                          authentication
                                                                                              .getAuthenticatedDate());
            mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
            return mutableAuthentication;
        }
        return authentication;
    }
}
