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
package org.jasig.cas.services;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a persistent id as username for anonymous service access.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class AnonymousRegisteredServiceUsernameAttributeProvider implements RegisteredServiceUsernameAttributeProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Encoder to generate PseudoIds. */
    @NotNull
    private final PersistentIdGenerator persistentIdGenerator;
    
    /**
     * Instantiates a new default registered service username provider.
     *
     * @param persistentIdGenerator the persistent id generator
     */
    public AnonymousRegisteredServiceUsernameAttributeProvider(@NotNull final PersistentIdGenerator persistentIdGenerator) {
        this.persistentIdGenerator = persistentIdGenerator;
    }
    
    @Override
    public String resolveUsername(final Principal principal) {
        final String id = this.persistentIdGenerator.generate(principal);
        logger.debug("Resolved username [{}] for anonymous access", id);
        return id;
    }
}
