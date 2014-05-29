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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ReturnMappedAttributeReleasePolicy extends AbstractAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;
    
    private Map<String, String> allowedAttributes = Collections.emptyMap();
    
    /**
     * Sets the allowed attributes.
     *
     * @param allowed the allowed attributes.
     */
    public void setAllowedAttributes(final Map<String, String> allowed) {
        this.allowedAttributes = allowed;
    }
    
    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    protected Map<String, String> getAllowedAttributes() {
        return Collections.unmodifiableMap(this.allowedAttributes);
    }
    
    @Override
    protected Map<String, Object> getAttributesInternal(final Principal p) {
        final Map<String, Object> attributesToRelease = new HashMap<String, Object>(p.getAttributes().size());
        final Map<String, Object> resolvedAttributes = p.getAttributes();
        
        for (final String attribute : this.allowedAttributes.keySet()) {
            final Object value = resolvedAttributes.get(attribute);

            if (value != null) {
                final String mappedAttributeName = this.allowedAttributes.get(attribute);
                logger.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                        attribute, mappedAttributeName);
                attributesToRelease.put(mappedAttributeName, value);
            }
        }
        return attributesToRelease;
    }
}
