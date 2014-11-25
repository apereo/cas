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
package org.jasig.cas.services;

import java.io.Serializable;
import java.net.URL;

/**
 * Defines the proxying policy for a registered service.
 * While a service may be allowed proxying on a general level,
 * it may still want to restrict who is authorizes to receive
 * the proxy granting ticket. This interface defines the behavior
 * for both options.
 * 
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface RegisteredServiceProxyPolicy extends Serializable {

    /**
     * Determines whether the service is allowed proxy
     * capabilities. 
     *
     * @return true, if is allowed to proxy
     */
    boolean isAllowedToProxy();
    
    /**
     * Determines if the given proxy callback
     * url is authorized and allowed to
     * request proxy access.
     *
     * @param pgtUrl the pgt url
     * @return true, if url allowed.
     */
    boolean isAllowedProxyCallbackUrl(URL pgtUrl);
}
