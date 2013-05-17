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
package org.jasig.cas.validation;

import static org.junit.Assert.*;

import org.jasig.cas.TestUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class Cas20WithoutProxyingValidationSpecificationTests {

    private Cas20WithoutProxyingValidationSpecification validationSpecification;

    @Before
    public void setUp() throws Exception {
        this.validationSpecification = new Cas20WithoutProxyingValidationSpecification();
    }

    @Test
    public void testSatisfiesSpecOfTrue() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(true)));
    }

    @Test
    public void testNotSatisfiesSpecOfTrue() {
        this.validationSpecification.setRenew(true);
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    @Test
    public void testSatisfiesSpecOfFalse() {
        assertTrue(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false)));
    }

    @Test
    public void testDoesNotSatisfiesSpecOfFalse() {
        assertFalse(this.validationSpecification.isSatisfiedBy(TestUtils.getAssertion(false, new String[] {"test2"})));
    }

    @Test
    public void testSettingRenew() {
        final Cas20WithoutProxyingValidationSpecification validation = new Cas20WithoutProxyingValidationSpecification(
                true);
        assertTrue(validation.isRenew());
    }
}
