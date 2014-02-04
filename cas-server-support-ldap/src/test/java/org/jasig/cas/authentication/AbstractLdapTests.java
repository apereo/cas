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
package org.jasig.cas.authentication;

import java.util.Collection;

import org.jasig.cas.util.LdapTestUtils;
import org.jasig.cas.util.LdapUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Base class for LDAP tests that provision and deprovision directory data as part of test setup/teardown.
 * <p>
 * NOTE: The <code>enableLdapTests</code> system property must be set to execute tests that derive from this class.
 *
 * @author Marvin S. Addison
 */
public class AbstractLdapTests {

    private Resource usersLdif;

    private String usernameAttribute;

    private ConnectionFactory provisioningConnectionFactory;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String baseDn;

    protected String[] contextPaths;

    protected LdapTestUtils.DirectoryType directoryType;

    protected ApplicationContext context;

    protected Collection<LdapEntry> testEntries;

    protected boolean enableLdapTests;

    @Before
    public void setUp() throws Exception {
        // Environment check
        this.enableLdapTests = System.getProperty("enableLdapTests") != null;
        Assume.assumeTrue("enableLdapTests system property not set", this.enableLdapTests);

        this.context = new ClassPathXmlApplicationContext(this.contextPaths);
        this.baseDn = this.context.getBean("baseDn", String.class);
        this.usersLdif = this.context.getBean("usersLdif", Resource.class);
        this.usernameAttribute = this.context.getBean("usernameAttribute", String.class);
        this.provisioningConnectionFactory = this.context.getBean(
                "provisioningConnectionFactory", ConnectionFactory.class);
        this.testEntries = LdapTestUtils.readLdif(this.usersLdif, this.baseDn);
        final Connection connection = getConnection();
        try {
            connection.open();
            LdapTestUtils.createLdapEntries(connection, this.directoryType, this.testEntries);
        } finally {
            LdapUtils.closeConnection(connection);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!this.enableLdapTests) {
            return;
        }
        final Connection connection = getConnection();
        try {
            connection.open();
            LdapTestUtils.removeLdapEntries(connection, this.testEntries);
        } finally {
            LdapUtils.closeConnection(connection);
        }
    }

    /**
     * Gets a connection for provisioning/deprovisioning test data.
     *
     * @return LDAP connection.
     * @throws LdapException On errors.
     */
    protected Connection getConnection() throws LdapException {
        return provisioningConnectionFactory.getConnection();
    }

    protected String getUsername(final LdapEntry entry) {
        return entry.getAttribute(usernameAttribute).getStringValue();
    }
}
