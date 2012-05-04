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

import java.io.Serializable;

/**
 * Marker interface for credentials required to authenticate a principal.
 * <p>
 * The Credentials is an opaque object that represents the information a user
 * asserts proves that the user is who it says it is. In CAS, any information
 * that is to be presented for authentication must be wrapped (or implement) the
 * Credentials interface. Credentials can contain a userid and password, or a
 * Certificate, or an IP address, or a cookie value. Some credentials require
 * validation, while others (such as container based or Filter based validation)
 * are inherently trustworthy.
 * <p>
 * People who choose to implement their own Credentials object should take care that
 * any toString() they implement does not accidentally expose confidential information.
 * toString() can be called from various portions of the CAS code base, including logging
 * statements, and thus toString should never contain anything confidential or anything
 * that should not be logged.
 * <p>
 * Credentials objects that are included in CAS do NOT expose any confidential information.
 * 
 * @author William G. Thompson, Jr.
 * @version $Revision: 1.2 $ $Date: 2007/01/22 20:35:26 $
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface Credentials extends Serializable {
    // marker interface contains no methods
}
