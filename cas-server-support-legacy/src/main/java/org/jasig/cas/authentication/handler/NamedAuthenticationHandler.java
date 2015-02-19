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
package org.jasig.cas.authentication.handler;

/**
 * Named variant of CAS 3.0 {@link AuthenticationHandler} interface. This is deprecated in favor of
 * {@link org.jasig.cas.authentication.AuthenticationHandler}.
 *
 * @author Scott Battaglia
 * @deprecated The CAS 4.0 {@link org.jasig.cas.authentication.AuthenticationHandler} provides support for named
 * handlers, which makes this interface redundant.
 * @since 3.2.1
 *
 */
@Deprecated
public interface NamedAuthenticationHandler extends AuthenticationHandler {

    /**
     * Gets the name of this handler.
     *
     * @return the name
     */
    String getName();
}
