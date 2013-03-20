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
package org.jasig.cas.mock;

import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;

/**
 * Simple mock implementation of a service principal.
 *
 * @author Marvin S. Addison
 *
 */
public class MockService implements Service {

    private static final long serialVersionUID = 117438127028057173L;
    private boolean loggedOut = false;
    private String id;

    public MockService(final String id) {
        this.id = id;
    }

    public String getArtifactId() {
        return null;
    }

    public Response getResponse(final String ticketId) {
        return null;
    }

    public boolean logOutOfService(final String sessionIdentifier) {
        this.loggedOut = true;
        return false;
    }

    public boolean isLoggedOut() {
        return this.loggedOut;
    }

    public void setPrincipal(final Principal principal) {}

    public Map<String, Object> getAttributes() {
        return null;
    }

    public String getId() {
        return id;
    }

    public boolean matches(final Service service) {
        return true;
    }

}
