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

import org.jasig.cas.authentication.principal.Service;

public class MockRegisteredService extends AbstractRegisteredService {
    private static final long serialVersionUID = 4036877894594884813L;

    @Override
    public boolean matches(final Service service) {
        return true;
    }

    @Override
    public void setServiceId(final String id) {
        this.serviceId = id;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new MockRegisteredService();
    }
}
