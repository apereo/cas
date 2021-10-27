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

import org.jasig.cas.MessageDescriptor;
import org.jasig.cas.authentication.principal.Principal;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link HandlerResult} that describes the result of an authentication attempt.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface HandlerResult extends Serializable {

    /**
     * Gets handler name.
     *
     * @return the handler name
     */
    String getHandlerName();

    /**
     * Gets credential meta data.
     *
     * @return the credential meta data
     */
    CredentialMetaData getCredentialMetaData();

    /**
     * Gets principal.
     *
     * @return the principal
     */
    Principal getPrincipal();

    /**
     * Gets warnings.
     *
     * @return the warnings
     */
    List<MessageDescriptor> getWarnings();
}
