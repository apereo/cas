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
package org.jasig.cas.adaptors.cas;

import javax.servlet.ServletRequest;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Credentials class that maps to the paramters required by the Legacy CAS
 * password handler.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LegacyCasTrustedCredentials implements Credentials {

    private static final long serialVersionUID = 3618701894071892024L;

    private ServletRequest servletRequest;

    /**
     * @return Returns the servletRequest.
     */
    public ServletRequest getServletRequest() {
        return this.servletRequest;
    }

    /**
     * @param servletRequest The servletRequest to set.
     */
    public void setServletRequest(final ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}