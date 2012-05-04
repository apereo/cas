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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * <p>
 * The Authentication object represents a successful authentication request. It
 * contains the principal that the authentication request was made for as well
 * as the additional meta information such as the authenticated date and a map
 * of attributes.
 * </p>
 * <p>
 * An Authentication object must be serializable to permit persistance and
 * clustering.
 * </p>
 * <p>
 * Implementing classes must take care to ensure that the Map returned by
 * getAttributes is serializable by using a Serializable map such as HashMap.
 * </p>
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface Authentication extends Serializable {

    /**
     * Method to obtain the Principal.
     * 
     * @return a Principal implementation
     */
    Principal getPrincipal();

    /**
     * Method to retrieve the timestamp of when this Authentication object was
     * created.
     * 
     * @return the date/time the authentication occurred.
     */
    Date getAuthenticatedDate();

    /**
     * Attributes of the authentication (not the Principal).
     * 
     * @return the map of attributes.
     */
    Map<String, Object> getAttributes();
}
