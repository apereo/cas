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

package org.jasig.cas.util;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Misagh Moayyed
 */
public final class PrincipalUtils {
    /**
     * Instantiates a new Principal utils.
     */
    private PrincipalUtils() {}

    /**
     * Convert person attributes to principal attributes.
     * Obtains attributes first from the repository by calling
     * {@link org.jasig.services.persondir.IPersonAttributeDao#getPerson(String)}
     * and converts the results into a map of attributes that CAS can understand.
     *
     * @param id the person id to locate in the attribute repository
     * @param attributeRepository the attribute repository
     * @return the map of principal attributes
     */
    public static Map<String, Object> convertPersonAttributesToPrincipalAttributes(final String id, final IPersonAttributeDao attributeRepository) {
        final IPersonAttributes attrs = attributeRepository.getPerson(id);
        if (attrs == null) {
            return Collections.emptyMap();
        }

        final Map<String, List<Object>> attributes = attrs.getAttributes();
        if (attributes == null) {
            return Collections.emptyMap();
        }

        final Map<String, Object> convertedAttributes = new HashMap<String, Object>();
        for (final String key : attributes.keySet()) {
            final List<Object> values = attributes.get(key);
            convertedAttributes.put(key, values.size() == 1 ? values.get(0) : values);
        }
        return convertedAttributes;
    }

}
