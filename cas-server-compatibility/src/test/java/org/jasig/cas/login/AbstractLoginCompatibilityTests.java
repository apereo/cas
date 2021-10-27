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
package org.jasig.cas.login;

import java.io.IOException;

/**
 * Base class for Web compatibility tests around CAS login form.
 * @since 4.1
 */
public abstract class AbstractLoginCompatibilityTests extends AbstractCompatibilityTests {
    public static final String FORM_USERNAME = "username";
    public static final String FORM_PASSWORD = "password";

    /**
     * The name of the compatibility test configuration property the value of which
     * should be an alternate username to test logging into the CAS server.
     */
    public static final String ALTERNATE_USERNAME_PROPERTY = "credentials.alternate.username";

    /**
     * The name of the compatibility test configuration property the value of which should be
     * the correct password to go with that alternate username.
     */
    public static final String ALTERNATE_PASSWORD_PROPERTY = "credentials.alternate.password";

    public AbstractLoginCompatibilityTests() throws IOException {
        super();
    }

    public AbstractLoginCompatibilityTests(final String name) throws IOException {
        super(name);
    }

    protected String getAlternateUsername(){
        return getProperties().getProperty(ALTERNATE_USERNAME_PROPERTY);
    }

    protected String getAlternatePassword() {
        return getProperties().getProperty(ALTERNATE_PASSWORD_PROPERTY);
    }



}
