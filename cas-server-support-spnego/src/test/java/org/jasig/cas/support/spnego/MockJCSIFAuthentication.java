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
package org.jasig.cas.support.spnego;

import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;

import java.security.Principal;

/**
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 * @deprecated As of 4.1, the class name is abbreviated in a way that is not per camel-casing standards and will be renamed in the future.
 */
@Deprecated
public class MockJCSIFAuthentication extends Authentication {
    private final Principal principal;

    private final boolean valid;

    private final byte[] outToken = new byte[] {4, 5, 6};

    public MockJCSIFAuthentication(final boolean valid) {
        this.principal = new MockPrincipal("test");
        this.valid = valid;

    }

    @Override
    public byte[] getNextToken() {

        return this.valid ? this.outToken : null;
    }

    @Override
    public java.security.Principal getPrincipal() {

        return this.valid ? this.principal : null;
    }

    @Override
    public void process(final byte[] arg0) throws AuthenticationException {
        if (!this.valid) {
            throw new AuthenticationException("not valid");
        }
    }

}
