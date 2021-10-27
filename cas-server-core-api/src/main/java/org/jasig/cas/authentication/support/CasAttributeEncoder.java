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

package org.jasig.cas.authentication.support;

import org.jasig.cas.authentication.principal.Service;

import java.util.Map;

/**
 * An encoder that defines how a CAS attribute
 * is to be encoded and signed in the CAS
 * validation response. The collection of
 * attributes should not be mangled with and
 * filtered. All attributes will be released.
 * It is up to the implementations
 * to decide which attribute merits encrypting.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CasAttributeEncoder {

    /**
     * Encodes attributes that are ready to be released.
     * Specifically, this method tries to ensure that the
     * PGT and the credential password are correctly encrypted
     * before they are released. Attributes should not be filtered
     * and removed and it is assumed that all will be returned
     * back to the service.
     * @param attributes The attribute collection that is ready to be released
     * @param service the requesting service for which attributes are to be encoded
     * @return collection of attributes after encryption ready for release.
     * @since 4.1
     */
    Map<String, Object> encodeAttributes(Map<String, Object> attributes, Service service);

}
