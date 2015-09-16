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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseSpnegoKnownClientSystemsFilterAction}
 * and {@link HostNameSpnegoKnownClientSystemsFilterAction}.
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
public class AllSpnegoKnownClientSystemsFilterActionTest {

    @Test
    public void ensureRemoteIpShouldBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^192\\.158\\..+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.158.5.781");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureRemoteIpShouldNotBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^192\\.158\\..+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("193.158.5.781");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertNotEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureAltRemoteIpHeaderShouldBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^74\\.125\\..+", "alternateRemoteIp");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("555.555.555.555");
        req.addHeader("alternateRemoteIp", "74.125.136.102");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureHostnameShouldDoSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("\\w+\\.\\w+\\.\\w+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("74.125.136.102");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());

    }

    @Test
    public void ensureHostnameAndIpShouldDoSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("\\w+\\.\\w+\\.\\w+");
        action.setIpsToCheckPattern("74\\..+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("74.125.136.102");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());

    }

    @Test
    public void verifyIpMismatchWhenCheckingHostnameForSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("\\w+\\.\\w+\\.\\w+");
        action.setIpsToCheckPattern("14\\..+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("74.125.136.102");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().no(this).getId());

    }
}
