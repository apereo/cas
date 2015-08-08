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
package org.jasig.cas.support.wsfederation;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface provides a mechanism to alter the SAML attributes before they
 * are added the WsFederationCredentials and returned to CAS.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public interface WsFederationAttributeMutator extends Serializable {
    /**
     * modifyAttributes manipulates the attributes before they
     * are assigned to the credential.
     *
     * @param attributes the attribute returned by the IdP.
     */
    void modifyAttributes(Map<String, Object> attributes);
}
