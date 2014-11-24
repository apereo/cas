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

import java.io.Serializable;
import java.util.Map;

/**
 * Defines operations required for retrieving principal attributes.
 * @author Misagh Moayyed
 * @see org.jasig.cas.authentication.principal.PrincipalFactory
 * @since 4.1
 */
public interface PrincipalAttributesRepository extends Serializable {

    /**
     * Sets the attributes initially received.
     * Implementations may choose to cache these attributes
     * and return them again via {@link #getAttributes(String)}
     * or to entirely ignore them and return a fresh copy.
     *
     * @param id the identifier to which the attributes may be linked in the repository.
     *           This typically would be the principal id.
     * @param attributes the attributes
     */
    void setAttributes(String id, Map<String, Object> attributes);

    /**
     * Gets attributes for the given principal id.
     *
     * @param id the id
     * @return the attributes
     */
    Map<String, Object> getAttributes(String id);
}
