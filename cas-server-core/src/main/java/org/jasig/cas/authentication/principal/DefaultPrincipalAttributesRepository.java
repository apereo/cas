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

package org.jasig.cas.authentication.principal;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link PrincipalAttributesRepository}
 * that just returns the attributes as it receives them.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepository implements PrincipalAttributesRepository {
    private static final long serialVersionUID = -4535358847021241725L;

    private Map<String, Object> attributes;

    /**
     * Instantiates a new No op principal attribute repository.
     */
    public DefaultPrincipalAttributesRepository() {
        setAttributes(new HashMap<String, Object>());
    }

    /**
     * Instantiates a new Default principal attribute repository.
     *
     * @param attributes the attributes
     */
    public DefaultPrincipalAttributesRepository(final Map<String, Object> attributes) {
        setAttributes(attributes);
    }

    @Override
    public void setAttributes(@NotNull final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes(final String id) {
        return this.attributes;
    }
}
