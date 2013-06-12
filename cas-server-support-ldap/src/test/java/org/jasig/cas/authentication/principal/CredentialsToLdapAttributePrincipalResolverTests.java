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


import static org.junit.Assert.*;

import java.util.Properties;

import org.jasig.cas.RequiredConfigurationProfileValueSource;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Misagh Moayyed
 * @since 3.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-test.xml"})
@ProfileValueSourceConfiguration(RequiredConfigurationProfileValueSource.class)
@IfProfileValue(name = "authenticationConfig", value = "true")
public class CredentialsToLdapAttributePrincipalResolverTests {

    private static final Logger log = LoggerFactory.getLogger(CredentialsToLdapAttributePrincipalResolverTests.class);
    
    @Autowired
    private PrincipalResolver resolver;

    @Autowired
    @Qualifier("testCredentials")
    private Properties testCredentials;

    @Test
    public void testResolver() throws Exception {
        for (String username : testCredentials.stringPropertyNames()) {
            String[] values = testCredentials.get(username).toString().split("\\|");
            String password = values[0];

            final Credential cred = newCredentials(username, password);
            final Principal p = resolver.resolve(cred);
            if (p != null) {
                assertTrue(p.getAttributes().size() > 0);
                assertTrue(p.getAttributes().containsKey("commonName"));
                assertTrue(p.getAttributes().containsKey("name"));
            } else {
                log.warn("{} cannot be resolved. Verify the test settings to ensure the account is valid.", cred);
            }
        }
    }

    private Credential newCredentials(final String user, final String pass) {
        final UsernamePasswordCredential credentials = new UsernamePasswordCredential();
        credentials.setUsername(user);
        credentials.setPassword(pass);
        return credentials;
    }
}