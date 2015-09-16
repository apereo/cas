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

package org.jasig.cas.extension.clearpass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.jasig.cas.extension.clearpass.ClearPassController}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 3.5.0
 */
public class ClearPassControllerTests {

    private CacheManager manager;
    private Cache cache;
    private EhcacheBackedMap map;

    @Before
    public void prep() {
        this.manager = CacheManager.create();
        this.manager.addCache("cascache");
        this.cache = this.manager.getCache("cascache");
        this.map = new EhcacheBackedMap(this.cache);
    }

    @After
    public void shutdown() {
        this.manager.shutdown();
    }

    @Test
    public void verifyClearPassWithNoUsername() throws Exception {
        final ClearPassController controller = new ClearPassController(this.map);
        final ModelAndView mv = controller.handleRequestInternal(new MockHttpServletRequest(),
                new MockHttpServletResponse());
        assertEquals(mv.getViewName(), ClearPassController.DEFAULT_SERVICE_FAILURE_VIEW_NAME);
        assertTrue(mv.getModel().containsKey(ClearPassController.MODEL_FAILURE_DESCRIPTION));
    }

    @Test
    public void verifyClearPassWithUsernameMissingInCache() throws Exception {
        final ClearPassController controller = new ClearPassController(this.map);
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteUser("casuser");
        final ModelAndView mv = controller.handleRequestInternal(req,
                new MockHttpServletResponse());
        assertEquals(mv.getViewName(), ClearPassController.DEFAULT_SERVICE_FAILURE_VIEW_NAME);
        assertTrue(mv.getModel().containsKey(ClearPassController.MODEL_FAILURE_DESCRIPTION));
    }

    @Test
    public void verifyClearPassSuccess() throws Exception {
        final ClearPassController controller = new ClearPassController(this.map);
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteUser("casuser");
        this.map.put("casuser", "password");

        final ModelAndView mv = controller.handleRequestInternal(req,
                new MockHttpServletResponse());
        assertEquals(mv.getViewName(), ClearPassController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME);
        assertTrue(mv.getModel().containsKey(ClearPassController.MODEL_CLEARPASS));
        assertTrue(mv.getModel().containsValue("password"));
    }
}
