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

package org.jasig.cas.support.spnego.web.flow.client;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml"})
public class LdapSpnegoKnownClientSystemsFilterActionTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("provisioningConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    private SearchRequest searchRequest;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void ensureLdapAttributeShouldDoSpnego() {
        final LdapSpnegoKnownClientSystemsFilterAction action =
                new LdapSpnegoKnownClientSystemsFilterAction(this.connectionFactory,
                this.searchRequest, "mail");
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("localhost");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }
}
