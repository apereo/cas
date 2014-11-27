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
 * Compatibility tests for /serviceValidate .
 * Note that this extends Cas2ValidateCompatibilityTests, which provides
 * several tests that apply to both service validate and proxy validate.
 *
 * @since 3.0.0
 */
public class ServiceValidateCompatibilityTests
extends AbstractCas2ValidateCompatibilityTests {

    /**
     * @throws IOException
     */
    public ServiceValidateCompatibilityTests() throws IOException {
        super();
    }

    /**
     * @throws IOException
     */
    public ServiceValidateCompatibilityTests(final String name) throws IOException {
        super(name);
    }


    @Override
    protected String getValidationPath() {
        return "/serviceValidate";
    }

    // TODO add tests specific to /serviceValidate

}
