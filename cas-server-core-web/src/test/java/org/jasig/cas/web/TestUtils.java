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

package org.jasig.cas.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * This is {@link TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TestUtils {

    private TestUtils() {}

    public static MockRequestContext getContext() {
        return getContext(new MockHttpServletRequest());
    }

    public static MockRequestContext getContext(
            final MockHttpServletRequest request) {
        return getContext(request, new MockHttpServletResponse());
    }

    public static MockRequestContext getContext(
            final MockHttpServletRequest request,
            final MockHttpServletResponse response) {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        return context;
    }

}
