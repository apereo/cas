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
package org.jasig.cas.adaptors.ldap;

import org.apache.commons.io.IOUtils;
import org.jasig.cas.util.ldap.uboundid.InMemoryTestLdapDirectoryServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.Collection;

/**
 * Base class for LDAP tests that provision and deprovision DIRECTORY data as part of test setup/teardown.
 * <p>
 * NOTE: The <code>enableLdapTests</code> system property must be set to execute tests that derive from this class.
 *
 * @author Marvin S. Addison
 */
public abstract class AbstractLdapTests  {

    protected final XmlWebApplicationContext context;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static InMemoryTestLdapDirectoryServer DIRECTORY;

    public AbstractLdapTests(final String... configLocations) {
        this.context = new XmlWebApplicationContext();
        this.context.setServletContext(new MockServletContext());
        this.context.setConfigLocations(configLocations);
        this.context.refresh();
        this.context.start();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        final ClassPathResource properties = new ClassPathResource("ldap.properties");
        final ClassPathResource schema = new ClassPathResource("schema/standard-ldap.schema");

        DIRECTORY = new InMemoryTestLdapDirectoryServer(properties.getFile(),
                new ClassPathResource("ldif/ldap-base.ldif").getFile(),
                schema.getFile());
    }

    @AfterClass
    public static void tearDown() {
        IOUtils.closeQuietly(DIRECTORY);
    }

    protected Collection<LdapEntry> getEntries() {
        return DIRECTORY.getLdapEntries();
    }

    protected String getUsername(final LdapEntry entry) {
        final String unameAttr = this.context.getBean("usernameAttribute", String.class);
        return entry.getAttribute(unameAttr).getStringValue();
    }
}
