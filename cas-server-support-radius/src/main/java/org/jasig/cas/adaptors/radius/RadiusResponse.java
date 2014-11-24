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
package org.jasig.cas.adaptors.radius;

import java.util.List;

import net.jradius.packet.attribute.RadiusAttribute;

/**
 * Acts as a DTO, to carry the response returned by the
 * Radius authenticator in the event of a successful authentication,
 * and provides access to the response code as well as attributes
 * which may be used as authentication attributes.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class RadiusResponse {
    
    /** The code. */
    private final int code;
    
    /** The identifier. */
    private final int identifier;
    
    /** The attributes. */
    private final List<RadiusAttribute> attributes;
    
    /**
     * Instantiates a new radius response.
     *
     * @param code the code
     * @param identifier the identifier
     * @param attributes the attributes
     */
    public RadiusResponse(final int code, final int identifier, final List<RadiusAttribute> attributes) {
        this.code = code;
        this.identifier = identifier;
        this.attributes = attributes;
    }

    public int getCode() {
        return this.code;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public List<RadiusAttribute> getAttributes() {
        return this.attributes;
    }
}
