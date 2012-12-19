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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * AuthenticationHandler which authenticates Principal-bearing credentials.
 * Authentication must have occured at the time the Principal-bearing
 * credentials were created, so we perform no further authentication. Thus
 * merely by being presented a PrincipalBearingCredential, this handler returns
 * true.
 * 
 * @author Andrew Petro
 * @author Marvin S. Addison
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandler implements AuthenticationHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String name;

    public HandlerResult authenticate(final Credential credential) {
        log.debug("Trusting credential {}", credential);
        return new HandlerResult(this);
    }

    public boolean supports(final Credential credential) {
        return credential.getClass().equals(PrincipalBearingCredential.class);
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return StringUtils.hasText(this.name) ? this.name : getClass().getSimpleName();
    }
}