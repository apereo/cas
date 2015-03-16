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

package org.jasig.cas.authentication.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.view.CasViewConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link DefaultCasAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/core-context.xml", "/WEB-INF/cas-servlet.xml"})
public class DefaultCasAttributeEncoderTests {

    private Map<String, Object> attributes;

    @Autowired
    private ServicesManager servicesManager;

    @Before
    public void before() {
        this.attributes = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            this.attributes.put("attr" + i, newSingleAttribute("value" + i));
        }
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, newSingleAttribute("PGT-1234567"));
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, newSingleAttribute("PrincipalPassword"));
    }

    private Collection<String> newSingleAttribute(final String attr) {
        return Collections.singleton(attr);
    }

    @Test
    public void checkNoPublicKeyDefined() {
        final Service service = TestUtils.getService("testDefault");
        final CasAttributeEncoder encoder = new DefaultCasAttributeEncoder(this.servicesManager);
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, service);
        assertEquals(encoded.size(), this.attributes.size() - 2);
    }

    @Test
    public void checkAttributesEncodedCorrectly() {
        final Service service = TestUtils.getService("testencryption");
        final CasAttributeEncoder encoder = new DefaultCasAttributeEncoder(this.servicesManager);
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, service);
        assertEquals(encoded.size(), this.attributes.size());
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, encoded);
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, encoded);
    }

    private void checkEncryptedValues(final String name, final Map<String, Object> encoded) {
        final String v1 = ((Collection<?>) this.attributes.get(
                name)).iterator().next().toString();
        final String v2 = (String) encoded.get(name);
        assertNotEquals(v1, v2);
    }
}
